package ffc.airsync.api.services.healthcareservice

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoSyncDao
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.plus
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.HealthCareService
import org.bson.Document
import org.bson.types.ObjectId

class MongoHealthCareServiceDao : HealthCareServiceDao,
    MongoSyncDao<HealthCareService>("ffc", "healthcareservice") {

    init {
        try {
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
            dbCollection.createIndex(("orgIndex" equal 1) plus ("_id" equal 1), IndexOptions().unique(true))
            dbCollection.createIndex("patientIdIndex" equal 1, IndexOptions().unique(false))
            dbCollection.createIndex("providerIdIndex" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService {

        val insertVisit = visitInsertDocument(healthCareService, orgId)

        return dbCollection.ffcInsert(insertVisit)
    }

    override fun insert(healthCareService: List<HealthCareService>, orgId: String): List<HealthCareService> {
        val healCare = healthCareService.map {
            visitInsertDocument(it, orgId)
        }
        return dbCollection.ffcInsert(healCare)
    }

    override fun getByOrgId(orgId: String): List<HealthCareService> {
        return dbCollection.find("orgIndex" equal ObjectId(orgId))
            .map { it.toJson().parseTo<HealthCareService>() }.toList()
    }

    override fun get(id: String, orgId: String): HealthCareService? {
        val query = Document("_id", ObjectId(id))
            .append("orgIndex", ObjectId(orgId))
        val result = dbCollection.find(query).first()

        check(result != null) { "ไม่พบ health care service id $id" }
        return result.toJson().parseTo()
    }

    override fun getByPatientId(orgId: String, personId: String): List<HealthCareService> {
        val query = Document("patientIdIndex", ObjectId(personId))

        val result = dbCollection.find(query)

        result.forEach {
            check(it["orgIndex"].toString() == orgId) { "ไม่พบ health care service person id $personId" }
        }
        check(result != null) { "ไม่พบ health care service person id $personId" }
        return result.map { it.toJson().parseTo<HealthCareService>() }.toList()
    }

    override fun update(healthCareService: HealthCareService, orgId: String): HealthCareService {
        val query = Document("_id", ObjectId(healthCareService.id))
            .append("orgIndex", ObjectId(orgId))

        val oldHealthCareService: HealthCareService = dbCollection.find(query).firstOrNull()?.toJson()?.parseTo()
            ?: throw NoSuchElementException("การ Update ผิดพลาด ไม่มีข้อมูลเดิมอยู่")

        oldHealthCareService.link?.keys?.let {
            if (it.isNotEmpty())
                healthCareService.link!!.keys = it
        }

        val insertDocument = visitUpdateDocument(healthCareService, orgId)
        val resultUpdate = dbCollection.replaceOne(query, insertDocument)

        check(resultUpdate.isModifiedCountAvailable) { "พารามิตเตอร์การ Update ผิดพลาด" }
        val result = dbCollection.find(query).first()
        return result.toJson().parseTo()
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}

fun HealthCareServiceDao.visitInsertDocument(
    healthCareService: HealthCareService,
    orgId: String
): Document {
    val insertVisit = healthCareService.buildInsertBson()
    return buildVisit(insertVisit, orgId, healthCareService)
}

fun HealthCareServiceDao.visitUpdateDocument(
    healthCareService: HealthCareService,
    orgId: String
): Document {
    val insertVisit = healthCareService.buildUpdateBson()
    return buildVisit(insertVisit, orgId, healthCareService)
}

private fun buildVisit(
    insertVisit: Document,
    orgId: String,
    healthCareService: HealthCareService
): Document {
    insertVisit["orgIndex"] = ObjectId(orgId)
    insertVisit["patientIdIndex"] = ObjectId(healthCareService.patientId)
    insertVisit["providerIdIndex"] = ObjectId(healthCareService.providerId)
    return insertVisit
}
