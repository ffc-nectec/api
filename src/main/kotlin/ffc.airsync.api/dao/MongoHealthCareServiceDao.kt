package ffc.airsync.api.dao

import ffc.airsync.api.buildInsertBson
import ffc.airsync.api.ffcInsert
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.HealthCareService
import org.bson.Document
import org.bson.types.ObjectId

class MongoHealthCareServiceDao(host: String, port: Int) : HealthCareServiceDao,
    MongoAbsConnect(host, port, "ffc", "healthcareservice") {

    override fun insert(healthCareService: HealthCareService): HealthCareService {

        val insertVisit = healthCareService.buildInsertBson()

        return dbCollection.ffcInsert(insertVisit)
    }

    override fun find(id: String): HealthCareService? {
        val query = Document("_id", ObjectId(id))

        val result = dbCollection.find(query).first()

        if (result != null)
            return result.toJson().parseTo()
        return null
    }
}
