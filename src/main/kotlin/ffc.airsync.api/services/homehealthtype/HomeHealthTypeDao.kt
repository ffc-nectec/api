package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.healthcare.CommunityService.ServiceType

interface HomeHealthTypeDao : Dao {
    fun insert(homeHealthTypee: ServiceType): ServiceType
    fun insert(homeHealthTypee: List<ServiceType>): List<ServiceType> {
        return homeHealthTypee.map { insert(it) }
    }

    fun find(query: String): List<ServiceType>
}

val homeHealthTypes: HomeHealthTypeDao by lazy { MongoHomeHealthTypeDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
