package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.healthcare.HealthCareService

interface HealthCareServiceDao : Dao {
    fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService
    fun insert(healthCareService: List<HealthCareService>, orgId: String): List<HealthCareService> {
        return healthCareService.map { insert(it, orgId) }
    }

    fun get(orgId: String): List<HealthCareService>

    fun find(id: String, orgId: String): HealthCareService?
    fun findByPatientId(personId: String, orgId: String): List<HealthCareService>

    fun update(healthCareService: HealthCareService, orgId: String): HealthCareService
}

val healthCareServices: HealthCareServiceDao by lazy { MongoHealthCareServiceDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
