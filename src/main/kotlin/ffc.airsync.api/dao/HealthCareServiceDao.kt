package ffc.airsync.api.dao

import ffc.entity.healthcare.HealthCareService

interface HealthCareServiceDao : Dao {
    fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService
    fun insert(healthCareService: List<HealthCareService>, orgId: String): List<HealthCareService> {
        return healthCareService.map { insert(it, orgId) }
    }

    fun find(id: String, orgId: String): HealthCareService?

    fun update(healthCareService: HealthCareService, orgId: String): HealthCareService
}
