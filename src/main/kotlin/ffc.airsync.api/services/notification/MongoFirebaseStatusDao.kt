package ffc.airsync.api.services.notification

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.equal
import ffc.entity.Entity
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId

class MongoFirebaseStatusDao(
    host: String,
    port: Int
) : FirebaseStatusDao, MongoAbsConnect(host, port, "ffc", "firebasestatus") {

    init {
        try {
            dbCollection.createIndex(("orgIndex" equal 1).append("_id", 1), IndexOptions().unique(true))
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun syncData(orgId: String, limitOutput: Int): List<Entity> {
        return dbCollection.find("orgIndex" equal ObjectId(orgId)).limit(limitOutput).map {
            it.toJson().parseTo<Entity>()
        }.toList()
    }

    override fun insert(orgId: String, entityId: String) {
        val docInsert = Document.parse(Entity(entityId).toJson())
            .append("_id", ObjectId(entityId))
            .append("orgIndex", ObjectId(orgId))
        dbCollection.insertOne(docInsert)
    }

    override fun confirmSuccess(orgId: String, entityId: String) {
        dbCollection.deleteOne(("_id" equal ObjectId(entityId)).append("orgIndex", ObjectId(orgId)))
    }
}
