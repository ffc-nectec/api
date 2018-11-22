package ffc.airsync.api.services.specialpp

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
import ffc.entity.healthcare.SpecialPP
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test

class MongoSpecialPpTypeTest {

    lateinit var dao: SpecialPpDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    val ppType = SpecialPP.PPType("I3234", "เยี่ยมเบาตัว")

    @Before
    fun setUp() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = MongoSpecialPpType(serverAddress.hostString, serverAddress.port)
        dao.insert(ppType)
    }

    @Test
    fun get() {
        dao.get("I3234").name `should be equal to` "เยี่ยมเบาตัว"
    }

    @Test(expected = NoSuchElementException::class)
    fun getFall() {
        dao.get("xxx")
    }

    @Test
    fun query() {
        dao.query("323").first().name `should be equal to` "เยี่ยมเบาตัว"
    }

    @Test(expected = NoSuchElementException::class)
    fun queryFall() {
        dao.query("sadf").size `should be equal to` 0
    }
}
