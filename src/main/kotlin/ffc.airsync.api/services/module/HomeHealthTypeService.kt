package ffc.airsync.api.services.module

import ffc.airsync.api.getResourceAs
import ffc.entity.healthcare.CommunityServiceType

object HomeHealthTypeService {

    fun query(query: String): List<CommunityServiceType> {
        return homeHealtyTypeDao.find(query)
    }

    fun init() {
        if (query("").count() < 5) {
            homeHealtyTypeDao.insert(getResourceAs<List<CommunityServiceType>>("HomeHealthType.json"))
        }
    }
}
