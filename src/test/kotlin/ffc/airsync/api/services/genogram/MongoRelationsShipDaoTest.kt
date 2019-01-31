package ffc.airsync.api.services.genogram

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.services.person.MongoPersonDao
import ffc.airsync.api.services.person.PersonDao
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import ffc.entity.update
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoRelationsShipDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: GenoGramDao
    lateinit var daoPerson: PersonDao
    lateinit var somChai: Person
    lateinit var somYing: Person
    lateinit var rabbit: Person

    val `สมชาย` = Person().apply {
        identities.add(ThaiCitizenId("1231233123421"))
        prename = "นาย"
        firstname = "สมชาย"
        lastname = "โคตรกระบือ"
        sex = Person.Sex.MALE
        birthDate = LocalDate.now().minusYears(20)
        chronics.add(Chronic(Icd10("fair", "dxabc00x")))
        chronics.add(Chronic(Icd10("fair", "abcffe982")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "12345678901"
    }
    val `สมหญิง` = Person().apply {
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
        houseId = "11111111111"
    }
    val `กระต่าย` = Person().apply {
        identities.add(ThaiCitizenId("1122399087432"))
        prename = "นางสาว"
        firstname = "กระต่าย"
        lastname = "สุดน่ารัก"
        sex = Person.Sex.FEMALE
        birthDate = LocalDate.now().minusYears(22)
        chronics.add(Chronic(Icd10("sleep", "I10")))
        chronics.add(Chronic(Icd10("god", "I11")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "99887744998"
    }

    @Before
    fun initDb() {

        daoPerson = MongoPersonDao()
        dao = MongoRelationsShipDao()

        somChai = daoPerson.insert(ORG_ID, `สมชาย`)
        somYing = daoPerson.insert(ORG_ID, `สมหญิง`)
        rabbit = daoPerson.insert(ORG_ID, `กระต่าย`)

        somChai.relationships.add(Person.Relationship(Person.Relate.Child, somYing))
        somYing.relationships.add(Person.Relationship(Person.Relate.Father, somChai))
        daoPerson.update(ORG_ID, somChai)
        daoPerson.update(ORG_ID, somYing)
    }

    @Test
    fun get() {
        val dogRelation = dao.get(ORG_ID, somChai.id).first()

        dogRelation.id `should be equal to` somYing.id
        dogRelation.relate `should equal` Person.Relate.Child
    }

    @Test
    fun update() {
        somChai.update {
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
        }
        val dogUpdate = dao.update(ORG_ID, somChai.id, somChai.relationships)

        rabbit.update {
            relationships.add(Person.Relationship(Person.Relate.Father, somChai))
        }
        val rabbitUpdate = dao.update(ORG_ID, rabbit.id, rabbit.relationships)

        dogUpdate.first().id `should be equal to` somYing.id
        dogUpdate.last().id `should be equal to` rabbit.id

        rabbitUpdate.first().id `should be equal to` somChai.id
        rabbitUpdate.last().id `should be equal to` somChai.id
    }

    /**
     * สมชาย > สมหญิง > กระต่าย
     */
    @Test
    fun collectGenogram() {
        somYing.update {
            addRelationship(Pair(Person.Relate.Child, rabbit))
        }
        daoPerson.update(ORG_ID, somYing)

        rabbit.update {
            addRelationship(Pair(Person.Relate.Mother, somYing))
        }
        daoPerson.update(ORG_ID, rabbit)
        val rela = dao.collectGenogram(ORG_ID, somChai.id)
        rela.count() `should be equal to` 3
    }

    @Test
    fun removeOrgIdAndUpdate() {
        dao.get(ORG_ID, somChai.id).isNotEmpty() `should be equal to` true
        dao.removeByOrgId(ORG_ID)
        dao.get(ORG_ID, somChai.id).isEmpty() `should be equal to` true
        dao.update(ORG_ID, somChai.id, somChai.relationships)
        dao.get(ORG_ID, somChai.id).isNotEmpty() `should be equal to` true
    }

    @Test
    fun insertBlock() {
        dao.removeByOrgId(ORG_ID)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to สมชาย.relationships,
            somYing.id to `สมหญิง`.relationships,
            rabbit.id to `กระต่าย`.relationships
        )

        val resultBlock = dao.insertBlock(ORG_ID, 2, inputBlock)

        resultBlock.size `should be equal to` 3
    }

    @Test
    fun getBlock() {
        dao.removeByOrgId(ORG_ID)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to สมชาย.relationships,
            somYing.id to `สมหญิง`.relationships,
            rabbit.id to `กระต่าย`.relationships
        )
        dao.insertBlock(ORG_ID, 2, inputBlock)

        val resultBlock = dao.getBlock(ORG_ID, 2)
        resultBlock.size `should be equal to` 3
    }

    @Test
    fun confirmBlock() {
        dao.removeByOrgId(ORG_ID)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to somChai.relationships,
            somYing.id to somYing.relationships,
            rabbit.id to rabbit.relationships
        )
        dao.insertBlock(ORG_ID, 2, inputBlock)
        dao.confirmBlock(ORG_ID, 2)

        val resultBlock = dao.getBlock(ORG_ID, 2)
        resultBlock.size `should be equal to` 0

        dao.get(ORG_ID, somChai.id).isNotEmpty() `should be equal to` true
    }

    @Test
    fun unConfirmBlock() {
        dao.removeByOrgId(ORG_ID)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to somChai.relationships,
            somYing.id to somYing.relationships,
            rabbit.id to rabbit.relationships
        )
        dao.insertBlock(ORG_ID, 2, inputBlock)
        dao.unConfirmBlock(ORG_ID, 2)

        val resultBlock = dao.getBlock(ORG_ID, 2)
        resultBlock.size `should be equal to` 0

        dao.get(ORG_ID, somChai.id).isEmpty() `should be equal to` true
    }
}
