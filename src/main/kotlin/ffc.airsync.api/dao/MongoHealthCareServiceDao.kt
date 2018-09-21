package ffc.airsync.api.dao

import ffc.airsync.api.buildInsertBson
import ffc.airsync.api.buildUpdateBson
import ffc.airsync.api.ffcInsert
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.HealthCareService
import org.bson.Document
import org.bson.types.ObjectId

class MongoHealthCareServiceDao(host: String, port: Int) : HealthCareServiceDao,
    MongoAbsConnect(host, port, "ffc", "healthcareservice") {
    override fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService {
        val insertVisit = healthCareService.buildInsertBson()
            .append("orgId", orgId)

        return dbCollection.ffcInsert(insertVisit)
    }

    override fun find(id: String, orgId: String): HealthCareService? {
        val query = Document("_id", ObjectId(id))
            .append("orgId", orgId)
        val result = dbCollection.find(query).first()

        check(result != null) { "ไม่พบ health care service id $id" }
        return result.toJson().parseTo()
    }

    override fun update(healthCareService: HealthCareService, orgId: String): HealthCareService {
        val query = Document("_id", ObjectId(healthCareService.id))
            .append("orgId", orgId)
        val updateDocument = healthCareService.buildUpdateBson()
        val resultUpdate = dbCollection.updateOne(query, updateDocument)

        check(resultUpdate.isModifiedCountAvailable) { "พารามิตเตอร์การ Update ผิดพลาด" }
        val result = dbCollection.find(query).first()
        return result.toJson().parseTo()
    }
}
