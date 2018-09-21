package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.Person
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
        person.name `should be equal to` "นายสมชาย โคตรกระบือ"
    }

    @Test
    fun listInsert() {
        val persons = dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        persons.count() `should be equal to` 2
        persons[0].isTempId `should be equal to` false
        persons[0].name `should be equal to` "นางสาวสมหญิง สมบูรณ์จิต"
        persons[1].isTempId `should be equal to` false
        persons[1].name `should be equal to` "นายสมชาย โคตรกระบือ"
    }

    @Test
    fun findByOrgId() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        val persons = dao.findByOrgId(ORG_ID)
        persons[0].isTempId `should be equal to` false
        persons[0].name `should be equal to` "นางสาวสมหญิง สมบูรณ์จิต"
        persons[1].isTempId `should be equal to` false
        persons[1].name `should be equal to` "นายสมชาย โคตรกระบือ"
    }

    @Test
    fun getPeopleInHouse() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        dao.getPeopleInHouse("12345678901")!!.first().name `should be equal to` "นายสมชาย โคตรกระบือ"
        dao.getPeopleInHouse("11111111111")!!.first().name `should be equal to` "นางสาวสมหญิง สมบูรณ์จิต"
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
        dao.find("โคตร", ORG_ID).first().name `should be equal to` "นายสมชาย โคตรกระบือ"
        dao.find("สมชาย", ORG_ID).first().name `should be equal to` "นายสมชาย โคตรกระบือ"
    }

    @Test
    fun findByThaiCitizenId() {
        dao.insert(ORG_ID, arrayListOf<Person>().apply {
            add(missCat)
            add(misterDog)
        })

        dao.find("2123455687675", ORG_ID).count() `should be equal to` 1
        dao.find("2123455687675", ORG_ID).first().name `should be equal to` "นางสาวสมหญิง สมบูรณ์จิต"
        dao.find("1231233123421", ORG_ID).first().name `should be equal to` "นายสมชาย โคตรกระบือ"
    }
}
