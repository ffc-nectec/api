package ffc.airsync.api.services.person

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Disease
import ffc.entity.update
import ffc.entity.util.generateTempId
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoPersonTest {
    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: PersonDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer
    val misterDog = Person().apply {
        identities.add(ThaiCitizenId("1231233123421"))
        prename = "นาย"
        firstname = "สมชาย"
        lastname = "โคตรกระบือ"
        sex = Person.Sex.MALE
        birthDate = LocalDate.now().minusYears(20)
        chronics.add(Chronic(Disease(generateTempId(), "fair", "dxabc00x")))
        chronics.add(Chronic(Disease(generateTempId(), "fair", "abcffe982")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "12345678901"
    }
    val missCat = Person().apply {
        identities.add(ThaiCitizenId("2123455687675"))
        prename = "นางสาว"
        firstname = "สมหญิง"
        lastname = "สมบูรณ์จิต"
        sex = Person.Sex.FEMALE
        birthDate = LocalDate.now().minusYears(27)
        chronics.add(Chronic(Disease(generateTempId(), "floor", "I10")))
        chronics.add(Chronic(Disease(generateTempId(), "fary", "I11")))
        link = Link(System.JHICS)
        link!!.isSynced = true
        houseId = "11111111111"
    }
    val missRabbit = Person().apply {
        identities.add(ThaiCitizenId("1122399087432"))
        prename = "นางสาว"
        firstname = "กระต่าย"
        lastname = "สุดน่ารัก"
        sex = Person.Sex.FEMALE
        birthDate = LocalDate.now().minusYears(22)
        chronics.add(Chronic(Disease(generateTempId(), "sleep", "I10")))
        chronics.add(Chronic(Disease(generateTempId(), "god", "I11")))
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
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = MongoPersonDao(serverAddress.hostString, serverAddress.port)
        dao.insert(ORG_ID, personFromJson)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insert() {
        val person = dao.insert(ORG_ID, misterDog)

        person.isTempId `should be equal to` false
        person.firstname `should be equal to` "สมชาย"
    }

    @Test
    fun listInsert() {
        val persons = dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        persons.count() `should be equal to` 2
        persons[0].isTempId `should be equal to` false
        persons[0].lastname `should be equal to` "สมบูรณ์จิต"
        persons[1].isTempId `should be equal to` false
        persons[1].firstname `should be equal to` "สมชาย"
    }

    @Test
    fun update() {
        val missRabbit = dao.insert(ORG_ID, missRabbit)
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
            add(missCat)
            add(misterDog)
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
            add(missCat)
            add(misterDog)
        })

        dao.getPeopleInHouse(ORG_ID, "12345678901").first().firstname `should be equal to` "สมชาย"
        dao.getPeopleInHouse(ORG_ID, "11111111111").first().lastname `should be equal to` "สมบูรณ์จิต"
    }

    @Test
    fun removeGroupByOrg() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        dao.remove(ORG_ID)
        val persons = dao.findByOrgId(ORG_ID)
        persons.count() `should be equal to` 0
    }

    @Test
    fun getPerson() {
        val insert = dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        }).first()
        val result = dao.getPerson(ORG_ID, insert.id)

        result.id `should be equal to` insert.id
        result.lastname `should be equal to` insert.lastname
    }

    @Test
    fun findByName() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
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
            add(missCat)
            add(misterDog)
        })

        dao.find("2123455687675", ORG_ID).count() `should be equal to` 1
        dao.find("2123455687675", ORG_ID).first().firstname `should be equal to` "สมหญิง"
        dao.find("1231233123421", ORG_ID).first().lastname `should be equal to` "โคตรกระบือ"
    }

    @Test
    fun findICD10() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
            add(missRabbit)
        })
        val result = dao.findByICD10(ORG_ID, "I11")

        result.count() `should be equal to` 2
        result[0].firstname `should be equal to` "สมหญิง"
        result[1].lastname `should be equal to` "สุดน่ารัก"
    }

    @Test
    fun syncData() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
            add(missRabbit)
        })

        dao.syncData(ORG_ID).count() `should be equal to` 2
    }

    @Test
    fun hashCodeTest() {
        misterDog.hashCode() `should be equal to` misterDog.hashCode()
        misterDog.hashCode() `should not be equal to` missCat.hashCode()
    }
}
