package ffc.airsync.api.services

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import com.mongodb.ServerAddress
import ffc.airsync.api.getLogger
import java.net.InetSocketAddress

class MongoDbConnector {

    companion object {

        var option = MongoClientOptions.builder()
            .connectionsPerHost(2)
            .maxConnectionIdleTime(30 * 1000)
            .connectTimeout(30 * 1000)
            .socketTimeout(5 * 1000)

        private var _client: MongoClient? = null
        internal val client: MongoClient
            get() = _client!!

        fun initialize() {
            val logger = this.getLogger()
            val systemEnv = System.getenv("MONGODB_URI")
            if (!systemEnv.isNullOrBlank()) {
                logger.debug("Run wiht system env MONGODB_URI")
                logger.trace("MONGODB_URI = $systemEnv")
                connect(systemEnv)
            } else {
                logger.debug("Run wiht local host $DEFAULT_MONGO_HOST port $DEFAULT_MONGO_PORT")
                connect(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT)
            }
        }

        fun connect(address: InetSocketAddress) {
            connect(address.hostName, address.port)
        }

        fun connect(host: String, port: Int) {
            _client = MongoClient(ServerAddress(host, port), option.build())
        }

        fun connect(uri: String) {
            _client = MongoClient(MongoClientURI(uri, option))
        }

        fun close() {
            _client?.close()
        }
    }
}
