package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Disease
import ffc.entity.util.generateTempId
import org.amshove.kluent.`should be equal to`
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoPersonTest {
    private val ORG_ID = "abcdeff"
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
        bundle["houseId"] = "12345678901"
        link = Link(System.JHICS)
        link!!.isSynced = false
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
        bundle["houseId"] = "11111111111"
        link = Link(System.JHICS)
        link!!.isSynced = true
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
        bundle["houseId"] = "99887744998"
        link = Link(System.JHICS)
        link!!.isSynced = false
    }

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = DaoFactory().persons(serverAddress.hostString, serverAddress.port)
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
    fun findByOrgId() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })
        val persons = dao.findByOrgId(ORG_ID)
        persons[0].isTempId `should be equal to` false
        persons[0].lastname `should be equal to` "สมบูรณ์จิต"
        persons[1].isTempId `should be equal to` false
        persons[1].firstname `should be equal to` "สมชาย"
    }

    @Test
    fun getPeopleInHouse() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        dao.getPeopleInHouse("12345678901")!!.first().firstname `should be equal to` "สมชาย"
        dao.getPeopleInHouse("11111111111")!!.first().lastname `should be equal to` "สมบูรณ์จิต"
    }

    @Test
    fun removeGroupByOrg() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        dao.removeGroupByOrg(ORG_ID)
        val persons = dao.findByOrgId(ORG_ID)
        persons.count() `should be equal to` 0
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

        dao.syncCloudFilter(ORG_ID).count() `should be equal to` 2
    }
}
