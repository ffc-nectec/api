package ffc.airsync.api.services.analytic

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.resourceFile
import ffc.airsync.api.services.person.MongoPersonDao
import ffc.airsync.api.services.person.PersonDao
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import ffc.entity.healthcare.analyze.HealthAnalyzer
import ffc.entity.healthcare.analyze.HealthIssue.Issue
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoAnalyticDAOTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: AnalyticDAO
    lateinit var personDao: PersonDao
    lateinit var somChai: Person
    lateinit var somYing: Person

    val analytic1 = resourceFile("analytic.json").parseTo<HealthAnalyzer>()
    val analytic2 = resourceFile("analytic2.json").parseTo<HealthAnalyzer>()
    val analyticNotDM = resourceFile("analyticNotDM.json").parseTo<HealthAnalyzer>()
    val analyticNotDMHT = resourceFile("analyticNotDMHT.json").parseTo<HealthAnalyzer>()
    val analyticNotHT = resourceFile("analyticNotHT.json").parseTo<HealthAnalyzer>()

    @Before
    fun setUp() {
        dao = MongoAnalyticDAO()
        personDao = MongoPersonDao()

        somChai = personDao.insert(ORG_ID, สมชาย)
        somYing = personDao.insert(ORG_ID, `สมหญิง`)
    }

    @Test
    fun insert() {
        with(dao.insert(ORG_ID, somChai.id, analytic1)) {
            result.forEach { key, value ->
                value.issue `should equal` analytic1.result[key]!!.issue
            }
        }
    }

    @Test
    fun insertAndRepeat() {
        dao.insert(ORG_ID, somChai.id, analytic1)
        dao.insert(ORG_ID, somChai.id, analytic2)

        dao.query(ORG_ID, "").first().healthAnalyze!!.timestamp `should equal` analytic2.timestamp
    }

    @Test
    fun getByPersonId() {
        dao.insert(ORG_ID, somChai.id, analytic1)
        dao.insert(ORG_ID, somYing.id, analytic2)

        dao.getByPersonId(ORG_ID, somChai.id).result.forEach { key, value ->
            value.issue `should equal` analytic1.result[key]!!.issue
        }
    }

    @Test
    fun getByHouseId() {
        dao.insert(ORG_ID, somChai.id, analytic1)
        dao.insert(ORG_ID, somYing.id, analytic2)

        dao.getByHouseId(ORG_ID, somYing.houseId).first().healthAnalyze!!.result.forEach { key, value ->
            value.issue `should equal` analytic2.result[key]!!.issue
        }
    }

    @Test(expected = NoSuchElementException::class)
    fun deleteByPersonId() {
        dao.insert(ORG_ID, somChai.id, analytic1)
        dao.insert(ORG_ID, somYing.id, analytic2)
        dao.deleteByPersonId(ORG_ID, somChai.id)

        dao.getByPersonId(ORG_ID, somChai.id)
    }

    @Test(expected = NoSuchElementException::class)
    fun deleteByOrgId() {
        dao.insert(ORG_ID, somChai.id, analytic1)
        dao.insert(ORG_ID, somYing.id, analytic2)
        dao.removeByOrgId(ORG_ID)

        dao.getByPersonId(ORG_ID, somChai.id)
    }

    @Test
    fun queryFound() {
        dao.insert(ORG_ID, somChai.id, analytic1)
        dao.insert(ORG_ID, somYing.id, analytic2)

        val dmSearch = dao.query(ORG_ID, Issue.DM.toString())
        val dementiaSearch = dao.query(ORG_ID, Issue.DEMENTIA.toString())
        val depressiveSearch = dao.query(ORG_ID, Issue.DEPRESSIVE.toString())
        dmSearch.size `should be equal to` 2
        dmSearch.first().healthAnalyze!!.timestamp `should equal` analytic1.timestamp
        dmSearch.last().healthAnalyze!!.timestamp `should equal` analytic2.timestamp
        dementiaSearch.size `should be equal to` 2
        depressiveSearch.size `should be equal to` 0
    }

    @Test
    fun insertBlock() {
        val input = hashMapOf<String, HealthAnalyzer>()
        input[somChai.id] = analytic1
        input[somYing.id] = analytic2
        val result = dao.insertBlock(ORG_ID, 1, input)

        result.size `should be equal to` 2
        result[somChai.id]!!.timestamp `should equal` analytic1.timestamp
        result[somYing.id]!!.timestamp `should equal` analytic2.timestamp
    }

    @Test
    fun getBlock() {
        val input = hashMapOf<String, HealthAnalyzer>()
        input[somChai.id] = analytic1
        input[somYing.id] = analytic2
        dao.insertBlock(ORG_ID, 1, input)

        val result = dao.getBlock(ORG_ID, 1)

        result.size `should be equal to` 2
        result[somChai.id]!!.timestamp `should equal` analytic1.timestamp
        result[somYing.id]!!.timestamp `should equal` analytic2.timestamp
    }

    @Test
    fun confirmBlock() {
        val input = hashMapOf<String, HealthAnalyzer>()
        input[somChai.id] = analytic1
        input[somYing.id] = analytic2
        dao.insertBlock(ORG_ID, 1, input)

        dao.getBlock(ORG_ID, 1).size `should be equal to` 2
        dao.query(ORG_ID, "").size `should be equal to` 2
        dao.confirmBlock(ORG_ID, 1)

        dao.getBlock(ORG_ID, 1).size `should be equal to` 0
        dao.query(ORG_ID, "").size `should be equal to` 2
    }

    @Test
    fun unConfirmBlock() {
        val input = hashMapOf<String, HealthAnalyzer>()
        input[somChai.id] = analytic1
        input[somYing.id] = analytic2
        dao.insertBlock(ORG_ID, 1, input)

        dao.getBlock(ORG_ID, 1).size `should be equal to` 2
        dao.query(ORG_ID, "").size `should be equal to` 2
        dao.unConfirmBlock(ORG_ID, 1)

        dao.getBlock(ORG_ID, 1).size `should be equal to` 0
        dao.query(ORG_ID, "").size `should be equal to` 0
    }

    @Test
    fun smartQueryFoundOld() {
        dao.insert(ORG_ID, somChai.id, analyticNotDMHT)
        dao.insert(ORG_ID, somYing.id, analytic2)

        val result = dao.smartQuery(ORG_ID, "ผู้สูงอายุ")

        result.size `should be equal to` 1
        result.first().name `should be equal to` somChai.name
    }

    @Test
    fun smartQueryFoundDM() {
        dao.insert(ORG_ID, somYing.id, analyticNotHT)

        val result = dao.smartQuery(ORG_ID, "เบาหวาน")

        result.size `should be equal to` 1
        result.first().name `should be equal to` somYing.name
    }

    @Test
    fun smartQueryNotFoundDM() {
        dao.insert(ORG_ID, somYing.id, analyticNotDM)

        val result = dao.smartQuery(ORG_ID, "เบาหวาน")

        result.size `should be equal to` 0
    }

    @Test
    fun smartQueryFoundOldDM() {
        dao.insert(ORG_ID, somChai.id, analyticNotHT)
        dao.insert(ORG_ID, somYing.id, analytic2)

        val result = dao.smartQuery(ORG_ID, "ผู้สูงอายุที่เป็นโรคเบาหวาน")

        result.size `should be equal to` 1
        result.first().name `should be equal to` somChai.name
    }

    @Test
    fun smartQueryNotFoundOldDM() {
        dao.insert(ORG_ID, somYing.id, analytic2)

        val result = dao.smartQuery(ORG_ID, "ผู้สูงอายุที่เป็นโรคเบาหวาน")

        result.size `should be equal to` 0
    }
}

private val `สมชาย` = Person().apply {
    identities.add(ThaiCitizenId("1231233123421"))
    prename = "นาย"
    firstname = "สมชาย"
    lastname = "โคตรกระบือ"
    sex = Person.Sex.MALE
    birthDate = LocalDate.now().minusYears(70)
    chronics.add(Chronic(Icd10("fair", "dxabc00x")))
    chronics.add(Chronic(Icd10("fair", "abcffe982")))
    link = Link(System.JHICS)
    link!!.isSynced = false
    houseId = "3bbd7f5ebc920637b04c7791"
}
private val `สมหญิง` = Person().apply {
    identities.add(ThaiCitizenId("2123455687675"))
    prename = "นางสาว"
    firstname = "สมหญิง"
    lastname = "สมบูรณ์จิต"
    sex = Person.Sex.FEMALE
    birthDate = LocalDate.now().minusYears(27)
    chronics.add(Chronic(Icd10("floor", "I10")))
    chronics.add(Chronic(Icd10("fary", "I11")))
    link = Link(System.JHICS)
    link!!.isSynced = true
    houseId = "3bbd7f5ebc920637b04c7792"
}
