package ffc.airsync.api.services.sync

import ffc.airsync.api.services.healthcareservice.healthCareServices
import ffc.airsync.api.services.house.houses
import ffc.airsync.api.services.person.persons
import ffc.entity.Entity
import ffc.entity.healthcare.HealthCareService

object EntityService {
    fun getNonSyncData(orgId: String): List<Entity> {
        val syncData = arrayListOf<Entity>()

        syncData.addAll(persons.syncCloudFilter(orgId))
        syncData.addAll(houses.syncCloudFilter(orgId))
        syncData.addAll(healthCareServices.syncCloudFilter(orgId))

        return syncData
    }

    fun getHealthCareService(orgId: String): List<HealthCareService> {
        val result = healthCareServices.syncCloudFilter(orgId).map {
            healthCareServices.find(it.id, orgId) ?: HealthCareService("", "", "")
        }.toMutableList()

        result.removeIf { it.patientId.isBlank() }

        return result
    }
}