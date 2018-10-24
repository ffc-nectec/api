package ffc.airsync.api.services.notification

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
import org.amshove.kluent.`should be equal to`
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoFirebaseStatusDaoTest {
    lateinit var dao: FirebaseStatusDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer
    val id1 = ObjectId().toHexString()
    val id2 = ObjectId().toHexString()
    val id3 = ObjectId().toHexString()

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = MongoFirebaseStatusDao(serverAddress.hostString, serverAddress.port)

        dao.insert("5bbd7f5ebc920637b04c7796", id1)
        dao.insert("5bbd7f5ebc920637b04c7796", id2)
        dao.insert("5bbd7f5ebc920637b04c7797", id3)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun syncCloudFilter() {
        val result = dao.syncData("5bbd7f5ebc920637b04c7796")

        result.count() `should be equal to` 2
        result.first().id `should be equal to` id1
    }

    @Test
    fun confirmSuccess() {
        dao.confirmSuccess("5bbd7f5ebc920637b04c7796", id1)
        val result = dao.syncData("5bbd7f5ebc920637b04c7796")

        result.count() `should be equal to` 1
        result.first().id `should be equal to` id2
    }
}
