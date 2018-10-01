package ffc.airsync.api.services.notification

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.dao.MongoAbsConnect
import ffc.airsync.api.dao.equal
import ffc.entity.Entity
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId

class MongoFirebaseStatusDao(host: String, port: Int) : FirebaseStatusDao, MongoAbsConnect(host, port, "ffc", "firebasestatus") {

    init {
        try {
            dbCollection.createIndex(("orgId" equal 1).append("_id", 1), IndexOptions().unique(true))
            dbCollection.createIndex("orgId" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(orgId: String, entityId: String) {
        val docInsert = Document.parse(Entity(entityId).toJson())
            .append("_id", ObjectId(entityId))
            .append("orgId", orgId)
        dbCollection.insertOne(docInsert)
    }

    override fun confirmSuccess(orgId: String, entityId: String) {
        dbCollection.deleteOne(("_id" equal ObjectId(entityId)).append("orgId", orgId))
    }

    override fun syncCloudFilter(orgId: String, isSync: Boolean, limitOutput: Int): List<Entity> {
        return dbCollection.find("orgId" equal orgId).map {
            it.toJson().parseTo<Entity>()
        }.toList()
    }
}
