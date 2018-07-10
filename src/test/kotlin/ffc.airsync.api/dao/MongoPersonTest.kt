package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.Chronic
import ffc.entity.Person
import ffc.entity.ThaiCitizenId
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoPersonTest {
    val DATABASE_NAME = "ffcTest"
    val DB_COLLECTION = "person"
    private val ORG_ID = "abcdeff"

    lateinit var dao: PersonDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    lateinit var maxPersonUpdate: Person

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = DaoFactory().build(serverAddress.hostString, serverAddress.port)

        val maxPerson = Person().apply {
            identities.add(ThaiCitizenId("1231233123421"))
            prename = "นาย"
            firstname = "สมชาย"
            lastname = "โคตรกระบือ"
            sex = Person.Sex.MALE
            birthDate = LocalDate.now().minusYears(20)
            chronics.add(Chronic("dxabc00x"))
            chronics.add(Chronic("abcffe982"))
        }

        dao.insert(ORG_ID, maxPerson)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insert() {
    }
}