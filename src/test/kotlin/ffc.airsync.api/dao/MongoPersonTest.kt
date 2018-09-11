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

    val maxPerson = Person().apply {
        identities.add(ThaiCitizenId("1231233123421"))
        prename = "นาย"
        firstname = "สมชาย"
        lastname = "โคตรกระบือ"
        sex = Person.Sex.MALE
        birthDate = LocalDate.now().minusYears(20)
        chronics.add(Chronic(Disease(generateTempId(), "fair", "dxabc00x")))
        chronics.add(Chronic(Disease(generateTempId(), "fair", "abcffe982")))
    }

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = persons(serverAddress.hostString, serverAddress.port)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insert() {
        val person = dao.insert(ORG_ID, maxPerson)

        person.isTempId `should be equal to` false
        person.name `should be equal to` "นายสมชาย โคตรกระบือ"
    }
}
