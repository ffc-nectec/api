package ffc.airsync.api.services.module

import ffc.entity.Entity

object EntityService {
    fun getNonSyncData(orgId: String): List<Entity> {
        val syncData = arrayListOf<Entity>()

        syncData.addAll(persons.syncCloudFilter(orgId))
        syncData.addAll(houses.syncCloudFilter(orgId))
        syncData.addAll(healthCareServices.syncCloudFilter(orgId))

        return syncData
    }
}
