package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import ffc.airsync.api.printDebug
import ffc.entity.Entity
import ffc.entity.gson.parseTo
import org.bson.Document
import java.util.Arrays

typealias mongoInit = () -> Unit

abstract class MongoAbsConnect(
    private val host: String,
    private val port: Int,
    private val dbName: String,
    private val collection: String,
    private val mongoInitRun: mongoInit? = null
) : Dao {
    protected lateinit var dbCollection: MongoCollection<Document>
    val dbExecuted get() = dbCollection
    val mongoUrl = System.getenv("MONGODB_URI") + "?maxPoolSize=2&maxIdleTimeMS=20000&connectTimeoutMS=30000&socketTimeoutMS=30000"

    companion object {
        protected var mongoClient: MongoClient? = null
        fun setClient(mongo: MongoClient) {
            mongoClient = mongo
        }
    }

    init {
        connectToMongo()
    }

    protected fun getClient(): MongoClient? {
        return mongoClient
    }

    override fun syncCloudFilter(orgId: String, isSync: Boolean, limitOutput: Int): List<Entity> {
        val query = Document("link.isSynced", isSync)
        val result = this.dbExecuted.find(query).limit(limitOutput)

        if (result.count() < 1) return emptyList()
        val output = result.map {
            try {
                it.toJson().parseTo<Entity>()
            } catch (ex: Exception) {
                Entity()
            }
        }.toMutableList()
        output.removeIf { it.isTempId }

        return output
    }

    protected fun connectToMongo() {
        try {
            getMongoClient()
            getDbCollection()
        } catch (ex: Exception) {
            ex.printStackTrace()
            val exOut = javax.ws.rs.InternalServerErrorException("Mongo Error")
            exOut.stackTrace = ex.stackTrace
            throw exOut
        }

        mongoInitRun?.invoke()
    }

    private fun getDbCollection() {
        printDebug("Mongo getCollection $collection")
        printDebug("\tDebug mongoClient = $mongoClient")
        if (mongoUrl.isEmpty() || mongoUrl.startsWith("null")) {
            printDebug("\t MongoUrl is null")
            this.dbCollection = mongoClient!!.getDatabase(dbName).getCollection(collection)
        } else {
            printDebug("\t mongoUrl != null get systemenv ${System.getenv("MONGODB_DBNAME")}")
            val databaseName = System.getenv("MONGODB_DBNAME")
            this.dbCollection = mongoClient!!.getDatabase(databaseName).getCollection(collection)

            printDebug("\tSuccess create and connect db collection.")
        }
    }

    private fun getMongoClient() {
        printDebug("Mongo Uri $mongoUrl")
        if (mongoClient == null) {
            if (mongoUrl.isEmpty() || mongoUrl.startsWith("null")) {
                printDebug("Create mongo client localhost")
                mongoClient = MongoClient(Arrays.asList(ServerAddress(host, port))
                    /*,Arrays.asList(credential)*/)
                // printDebug("\t mongoUrl=nul")
            } else {
                printDebug("Create mongo clinet by uri")
                mongoClient = MongoClient(MongoClientURI(mongoUrl))
                printDebug("\tFinish create mongo clinet by uri.")
            }
            // mongoClient!!.writeConcern = WriteConcern.JOURNALED
            // instant = this
        }
    }

    protected fun disconnetMongo() {
        try {
            mongoClient!!.close()
        } catch (ex: Exception) {
        }
    }

    interface MongoSafeRun {
        fun run()
    }

    protected fun mongoSafe(codeWorking: MongoSafeRun) {
        try {
            // disconnetMongo()
            // connectToMongo()
            codeWorking.run()
            // disconnetMongo()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        } catch (ex: com.mongodb.MongoException) {
            ex.printStackTrace()
            throw ex
        }
    }
}
