package ffc.airsync.api.dao

import ffc.entity.healthcare.HealthCareService

interface HealthCareServiceDao : Dao {
    fun insert(healthCareService: HealthCareService): HealthCareService
    fun insert(healthCareService: List<HealthCareService>): List<HealthCareService> {
        val result = arrayListOf<HealthCareService>()
        healthCareService.forEach { result.add(insert(it)) }
        return result
    }

    fun find(id: String): HealthCareService?
}
