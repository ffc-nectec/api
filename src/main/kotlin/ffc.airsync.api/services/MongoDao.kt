package ffc.airsync.api.services

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Indexes
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.plus
import ffc.entity.Entity
import ffc.entity.gson.parseTo
import org.bson.Document
import org.bson.types.ObjectId

typealias mongoInit = () -> Unit

const val DEFAULT_MONGO_HOST = "127.0.0.1"
const val DEFAULT_MONGO_PORT = 27017

abstract class MongoDao(
    val dbName: String,
    val collection: String,
    mongoInitRun: mongoInit? = null
) : Dao {

    private val mongoClient: MongoClient
    protected lateinit var dbCollection: MongoCollection<Document>

    init {
        try {
            mongoClient = MongoDbConnector.client
            getDbCollection()
        } catch (ex: Exception) {
            getLogger().error(ex.message)
            val exOut = javax.ws.rs.InternalServerErrorException(ex.message)
            exOut.stackTrace = ex.stackTrace
            throw exOut
        }
        mongoInitRun?.invoke()
    }

    private fun getDbCollection() {
        val logger = getLogger()
        logger.debug("Mongo getCollection $collection")
        val databaseName = System.getenv("MONGODB_DBNAME")
        if (databaseName != null) {
            logger.debug("\t mongoUrl != null get systemenv $databaseName")
            this.dbCollection = mongoClient.getDatabase(databaseName).getCollection(collection)

            logger.debug("\tSuccess create and connect db collection.")
        } else {
            logger.debug("\t MongoUrl is null")
            this.dbCollection = mongoClient.getDatabase(dbName).getCollection(collection)
        }
    }

    override fun syncData(orgId: String, limitOutput: Int): List<Entity> {
        val result = dbCollection.find(
            ("link.isSynced" equal false) plus ("orgIndex" equal ObjectId(orgId))
        ).limit(limitOutput)

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

    fun createIndexById() {
        try {
            dbCollection.createIndex(Indexes.hashed("_id"))
        } catch (ignore: Exception) {
        }
    }
}
