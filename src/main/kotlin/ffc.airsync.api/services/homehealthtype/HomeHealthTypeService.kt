package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.getResourceAs
import ffc.entity.healthcare.CommunityService.ServiceType

object HomeHealthTypeService {
    fun query(query: String): List<ServiceType> {
        val communityServiceType = homeHealthTypes.find(query)
        communityServiceType.forEach {
            it.translation.clear()
        }
        return communityServiceType
    }

    fun init() {
        if (query("").count() < 5) {
            homeHealthTypes.insert(getResourceAs<List<ServiceType>>("HomeHealthType.json"))
        }
    }
}
