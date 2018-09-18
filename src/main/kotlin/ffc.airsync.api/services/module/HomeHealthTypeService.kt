package ffc.airsync.api.services.module

import ffc.airsync.api.getResourceAs
import ffc.entity.healthcare.CommunityServiceType

object HomeHealthTypeService {
    fun query(query: String): List<CommunityServiceType> {
        val communityServiceType = homeHealtyTypeDao.find(query)
        communityServiceType.forEach {
            it.translation.clear()
        }
        return communityServiceType
    }

    fun init() {
        if (query("").count() < 5) {
            homeHealtyTypeDao.insert(getResourceAs<List<CommunityServiceType>>("HomeHealthType.json"))
        }
    }
}
