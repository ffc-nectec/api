package ffc.airsync.api.services.person

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.resourceFile
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import ffc.entity.healthcare.analyze.HealthAnalyzer
import ffc.entity.healthcare.analyze.HealthIssue
import ffc.entity.update
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoPersonTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: PersonDao

    val `สมชาย` = Person().apply {
        identities.add(ThaiCitizenId("1231233123421"))
        prename = "นาย"
        firstname = "สมชาย"
        lastname = "โคตรกระบือ"
        sex = Person.Sex.MALE
        birthDate = LocalDate.now().minusYears(20)
        chronics.add(Chronic(Icd10("fair", "dxabc00x")))
        chronics.add(Chronic(Icd10("fair", "abcffe982")))
        healthAnalyze = resourceFile("analyticNotDM.json").parseTo<HealthAnalyzer>()
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
        healthAnalyze = resourceFile("analyticNotHT.json").parseTo<HealthAnalyzer>()
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
    val json = """
        {
  "identities": [
    {
      "type": "thailand-citizen-id",
      "id": "1111111111111"
    }
  ],
  "prename": "นางสาว",
  "firstname": "พรทิพา",
  "lastname": "โชคสูงเนิน",
  "chronics": [],
  "sex": "UNKNOWN",
  "birthDate": "1993-06-29",
  "link": {
    "isSynced": true,
    "lastSync": "2018-06-25T14:09:07.815+07:00",
    "system": "JHICS",
    "keys": {
      "pid": "1234560",
      "cid": "11014578451230"
    }
  },
  "id": "76f5dc0db6b247ce9d5241dda6557300",
  "type": "Person",
  "timestamp": "2018-06-25T14:09:07.815+07:00"
}
    """.trimIndent()
    lateinit var personFromJson: Person

    @Before
    fun initDb() {
        personFromJson = json.parseTo()

        dao = MongoPersonDao()
        dao.insert(ORG_ID, personFromJson)
    }

    @Test
    fun insert() {
        val person = dao.insert(ORG_ID, `สมชาย`)

        person.isTempId `should be equal to` false
        person.firstname `should be equal to` "สมชาย"
    }

    @Test
    fun listInsert() {
        val persons = dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })

        persons.count() `should be equal to` 2
        persons[0].isTempId `should be equal to` false
        persons[0].lastname `should be equal to` "สมบูรณ์จิต"
        persons[1].isTempId `should be equal to` false
        persons[1].firstname `should be equal to` "สมชาย"
    }

    @Test
    fun update() {
        val missRabbit = dao.insert(ORG_ID, `กระต่าย`)
        val person = dao.getPerson(ORG_ID, missRabbit.id)
        person.update {
            lastname = "สีขาว"
        }
        val personUpdate = dao.update(ORG_ID, person)

        personUpdate.lastname `should be equal to` "สีขาว"
    }

    @Test
    fun findByOrgId() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })
        val persons = dao.findByOrgId(ORG_ID)
        persons[1].isTempId `should be equal to` false
        persons[1].lastname `should be equal to` "สมบูรณ์จิต"
        persons[2].isTempId `should be equal to` false
        persons[2].firstname `should be equal to` "สมชาย"
    }

    @Test
    fun getPeopleInHouse() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })

        dao.getPeopleInHouse(ORG_ID, "12345678901").first().firstname `should be equal to` "สมชาย"
        dao.getPeopleInHouse(ORG_ID, "11111111111").first().lastname `should be equal to` "สมบูรณ์จิต"
    }

    @Test
    fun removeGroupByOrg() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })

        dao.remove(ORG_ID)
        val persons = dao.findByOrgId(ORG_ID)
        persons.count() `should be equal to` 0
    }

    @Test
    fun getPerson() {
        val insert = dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        }).first()
        val result = dao.getPerson(ORG_ID, insert.id)

        result.id `should be equal to` insert.id
        result.lastname `should be equal to` insert.lastname
    }

    @Test
    fun findByName() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })

        dao.find("สม", ORG_ID).count() `should be equal to` 2
        dao.find("โคตร", ORG_ID).count() `should be equal to` 1
        dao.find("โคตร", ORG_ID).first().firstname `should be equal to` "สมชาย"
        dao.find("สมชาย", ORG_ID).first().lastname `should be equal to` "โคตรกระบือ"
        dao.find("พา", ORG_ID).first().firstname `should be equal to` "พรทิพา"
    }

    @Test
    fun findByThaiCitizenId() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })

        dao.find("2123455687675", ORG_ID).count() `should be equal to` 1
        dao.find("2123455687675", ORG_ID).first().firstname `should be equal to` "สมหญิง"
        dao.find("1231233123421", ORG_ID).first().lastname `should be equal to` "โคตรกระบือ"
    }

    @Test
    fun findICD10() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
            add(`กระต่าย`)
        })
        val result = dao.findByICD10(ORG_ID, "I11")

        result.count() `should be equal to` 2
        result[0].firstname `should be equal to` "สมหญิง"
        result[1].lastname `should be equal to` "สุดน่ารัก"
    }

    @Test
    fun syncData() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
            add(`กระต่าย`)
        })

        dao.syncData(ORG_ID).count() `should be equal to` 2
    }

    @Test
    fun hashCodeTest() {
        `สมชาย`.hashCode() `should be equal to` `สมชาย`.hashCode()
        `สมชาย`.hashCode() `should not be equal to` `สมหญิง`.hashCode()
    }

    @Test
    fun getBlock() {
        dao.insertBlock(ORG_ID, 2, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
            add(`กระต่าย`)
        })

        dao.getBlock(ORG_ID, 2).count() `should be equal to` 3
    }

    @Test
    fun unconfirmBlock() {
        dao.insertBlock(ORG_ID, 2, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
            add(`กระต่าย`)
        })

        dao.unConfirmBlock(ORG_ID, 2)
        dao.getBlock(ORG_ID, 2).count() `should be equal to` 0
        dao.find("", ORG_ID).count() `should be equal to` 1
    }

    @Test
    fun confirmBlock() {
        dao.insertBlock(ORG_ID, 2, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
            add(`กระต่าย`)
        })

        dao.confirmBlock(ORG_ID, 2)

        dao.getBlock(ORG_ID, 2).count() `should be equal to` 0
        dao.find("", ORG_ID).count() `should be equal to` 4
    }

    @Test
    fun findHouseId() {
        val insert = dao.insertBlock(ORG_ID, 2, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
            add(`กระต่าย`)
        })

        with(dao.findHouseId(ORG_ID, insert.first().id)) {
            this `should be equal to` `สมหญิง`.houseId
        }

        with(dao.findHouseId(ORG_ID, insert.last().id)) {
            this `should be equal to` `กระต่าย`.houseId
        }
    }

    @Test
    fun getAnalyticByHouseId() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(`สมหญิง`)
            add(`สมชาย`)
        })
        val result = dao.getAnalyticByHouseId(ORG_ID, "12345678901")
        result.size `should be equal to` 3
        result[HealthIssue.Issue.HT]!!.first().haveIssue `should be equal to` false
        result[HealthIssue.Issue.DEMENTIA]!!.last().type `should be equal to` "HealthChecked"
    }
}
