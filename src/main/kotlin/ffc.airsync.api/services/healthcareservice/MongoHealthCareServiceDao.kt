package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
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

    override fun get(orgId: String): List<HealthCareService> {
        return dbCollection.find("orgId" equal orgId)
            .map { it.toJson().parseTo<HealthCareService>() }.toList()
    }

    override fun find(id: String, orgId: String): HealthCareService? {
        val query = Document("_id", ObjectId(id))
            .append("orgId", orgId)
        val result = dbCollection.find(query).first()

        check(result != null) { "ไม่พบ health care service id $id" }
        return result.toJson().parseTo()
    }

    override fun findByPatientId(personId: String, orgId: String): List<HealthCareService> {
        val query = Document("patientId", personId)
            .append("orgId", orgId)
        val result = dbCollection.find(query)

        check(result != null) { "ไม่พบ health care service person id $personId" }
        return result.map { it.toJson().parseTo<HealthCareService>() }.toList()
    }

    override fun update(healthCareService: HealthCareService, orgId: String): HealthCareService {
        val query = Document("_id", ObjectId(healthCareService.id))
            .append("orgId", orgId)
        val oldObject = dbCollection.find(query).first()!!
        val updateDocument = healthCareService.buildUpdateBson(oldObject)
        val resultUpdate = dbCollection.updateOne(query, updateDocument)

        check(resultUpdate.isModifiedCountAvailable) { "พารามิตเตอร์การ Update ผิดพลาด" }
        val result = dbCollection.find(query).first()
        return result.toJson().parseTo()
    }
}