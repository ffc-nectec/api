package ffc.airsync.api.dao

import com.mongodb.*
import ffc.airsync.api.printDebug
import java.util.*

abstract class MongoAbsConnect(val host: String, val port: Int, val dbName: String, val collection: String, val mongoInitRun: MongoInitRun = object : MongoInitRun {
    override fun run() {
    }
}) {

    protected lateinit var coll: DBCollection

    //protected var instant: MongoAbsConnect? = null
    val mongoUrl = System.getenv("MONGODB_URI") + "?maxPoolSize=2&maxIdleTimeMS=20000&connectTimeoutMS=30000&socketTimeoutMS=30000"


    companion object {
        protected var mongoClient: MongoClient? = null
    }


    init {
        connectToMongo(mongoInitRun)
    }


    interface MongoInitRun {
        fun run()
    }

    protected fun getClient(): MongoClient? {
        return mongoClient
    }

    protected fun connectToMongo(initRun: MongoInitRun) {

        try {
            getMongoClient()
            getDbCollection()
        } catch (ex: Exception) {
            ex.printStackTrace()
            val exOut = javax.ws.rs.InternalServerErrorException("Mongo Error")
            exOut.stackTrace = ex.stackTrace
            throw exOut
        }

        initRun.run()

    }

    private fun getDbCollection() {
        printDebug("Debug mongoClient = $mongoClient")
        if (mongoUrl.isEmpty() || mongoUrl.startsWith("null")) {
            this.coll = mongoClient!!.getDB(dbName).getCollection(collection)

        } else {
            printDebug("\t mongoUrl != null get systemenv ${System.getenv("MONGODB_DBNAME")}")

            this.coll = mongoClient!!.getDB(System.getenv("MONGODB_DBNAME")).getCollection(collection)
            printDebug("\tSuccess create and connect db collection.")
        }

    }

    private fun getMongoClient() {

        printDebug("Mongo Uri $mongoUrl")
        if (mongoClient == null) {
            if (mongoUrl.isEmpty() || mongoUrl.startsWith("null")) {
                printDebug("Create mongo client localhost")
                mongoClient = MongoClient(Arrays.asList(
                        ServerAddress(host, port)
                )/*,Arrays.asList(credential)*/)

                //printDebug("\t mongoUrl=nul")


            } else {
                printDebug("Create mongo clinet by uri")
                mongoClient = MongoClient(MongoClientURI(mongoUrl))
                printDebug("\tFinish create mongo clinet by uri.")
            }


            mongoClient!!.setWriteConcern(WriteConcern.JOURNALED)
            //instant = this
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
            //disconnetMongo()
            //connectToMongo()
            codeWorking.run()
            //disconnetMongo()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        } catch (ex: com.mongodb.MongoException) {
            ex.printStackTrace()
            throw ex
        }

    }


}
