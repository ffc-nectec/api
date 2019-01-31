package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.services.Dao
import ffc.airsync.api.services.Sync
import ffc.entity.healthcare.HealthCareService

interface HealthCareServiceDao : Dao, Sync<HealthCareService> {
    fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService
    fun insert(healthCareService: List<HealthCareService>, orgId: String): List<HealthCareService>

    fun getByOrgId(orgId: String): List<HealthCareService>

    fun get(id: String, orgId: String): HealthCareService?
    fun getByPatientId(orgId: String, personId: String): List<HealthCareService>

    fun update(healthCareService: HealthCareService, orgId: String): HealthCareService

    fun removeByOrgId(orgId: String)
}

val healthCareServices: HealthCareServiceDao by lazy {
    MongoHealthCareServiceDao()
}
