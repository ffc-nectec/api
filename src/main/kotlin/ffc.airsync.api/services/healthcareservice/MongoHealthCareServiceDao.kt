package ffc.airsync.api.services.healthcareservice

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoSyncDao
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.HealthCareService
import org.bson.Document
import org.bson.types.ObjectId

class MongoHealthCareServiceDao(host: String, port: Int) : HealthCareServiceDao,
    MongoSyncDao<HealthCareService>(host, port, "ffc", "healthcareservice") {

    init {
        try {
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService {

        ObjectId(healthCareService.patientId)
        ObjectId(healthCareService.providerId)

        val insertVisit = healthCareService.buildInsertBson()
            .append("orgIndex", ObjectId(orgId))

        return dbCollection.ffcInsert(insertVisit)
    }

    override fun insert(healthCareService: List<HealthCareService>, orgId: String): List<HealthCareService> {
        val healCare = healthCareService.map {
            ObjectId(it.patientId)
            ObjectId(it.providerId)
            it.buildInsertBson()
                .append("orgIndex", ObjectId(orgId))
        }
        return dbCollection.ffcInsert(healCare)
    }

    override fun get(orgId: String): List<HealthCareService> {
        return dbCollection.find("orgIndex" equal ObjectId(orgId))
            .map { it.toJson().parseTo<HealthCareService>() }.toList()
    }

    override fun find(id: String, orgId: String): HealthCareService? {
        val query = Document("_id", ObjectId(id))
            .append("orgIndex", ObjectId(orgId))
        val result = dbCollection.find(query).first()

        check(result != null) { "ไม่พบ health care service id $id" }
        return result.toJson().parseTo()
    }

    override fun findByPatientId(orgId: String, personId: String): List<HealthCareService> {
        val query = Document("patientId", personId)
            .append("orgIndex", ObjectId(orgId))
        val result = dbCollection.find(query)

        check(result != null) { "ไม่พบ health care service person id $personId" }
        return result.map { it.toJson().parseTo<HealthCareService>() }.toList()
    }

    override fun update(healthCareService: HealthCareService, orgId: String): HealthCareService {
        val query = Document("_id", ObjectId(healthCareService.id))
            .append("orgIndex", ObjectId(orgId))
        val oldObject = dbCollection.find(query).first()!!
        val updateDocument = healthCareService.buildUpdateBson(oldObject)
        val resultUpdate = dbCollection.updateOne(query, updateDocument)

        check(resultUpdate.isModifiedCountAvailable) { "พารามิตเตอร์การ Update ผิดพลาด" }
        val result = dbCollection.find(query).first()
        return result.toJson().parseTo()
    }

    override fun remove(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}
