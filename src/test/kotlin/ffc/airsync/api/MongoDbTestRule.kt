package ffc.airsync.api

import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoDbConnector
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.net.InetSocketAddress

class MongoDbTestRule : TestRule {

    override fun apply(base: Statement?, description: Description?) = object : Statement() {
        override fun evaluate() {
            try {
                before()
                base?.evaluate()
            } finally {
                after()
            }
        }
    }

    fun before() {
        server = MongoServer(MemoryBackend())
        address = server.bind()
        MongoDbConnector.connect(address)
    }

    fun after() {
        MongoDbConnector.close()
        server.shutdownNow()
    }

    private lateinit var server: MongoServer
    lateinit var address: InetSocketAddress
}
