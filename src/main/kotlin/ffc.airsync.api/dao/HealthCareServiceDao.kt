package ffc.airsync.api.dao

import ffc.entity.healthcare.HealthCareService

interface HealthCareServiceDao : Dao {
    fun insert(healthCareService: HealthCareService, orgId: String): HealthCareService
    fun insert(healthCareService: List<HealthCareService>, orgId: String): List<HealthCareService> {
        val result = arrayListOf<HealthCareService>()
        healthCareService.forEach { result.add(insert(it, orgId)) }
        return result
    }

    fun find(id: String, orgId: String): HealthCareService?
}
