package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import org.junit.rules.ExternalResource
import java.net.InetSocketAddress

class MongoTestRule : ExternalResource() {

    lateinit var client: MongoClient
    lateinit var server: MongoServer
    lateinit var address: InetSocketAddress

    override fun before() {
        super.before()
        server = MongoServer(MemoryBackend())
        address = server.bind()
        client = MongoClient(ServerAddress(address))
    }

    override fun after() {
        super.after()
        client.close()
        server.shutdownNow()
    }
}
