package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
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
        dao = DaoFactory().firebaseStauts(serverAddress.hostString, serverAddress.port)

        dao.insert("BNK119", id1)
        dao.insert("BNK119", id2)
        dao.insert("BNK120", id3)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun syncCloudFilter() {
        val result = dao.syncCloudFilter("BNK119")

        result.count() `should be equal to` 2
        result.first().id `should be equal to` id1
    }

    @Test
    fun confirmSuccess() {
        dao.confirmSuccess("BNK119", id1)
        val result = dao.syncCloudFilter("BNK119")

        result.count() `should be equal to` 1
        result.first().id `should be equal to` id2
    }
}
