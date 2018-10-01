package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.services.Dao
import ffc.airsync.api.services.DaoFactory
import ffc.entity.healthcare.CommunityServiceType

interface HomeHealthTypeDao : Dao {
    fun insert(homeHealthTypee: CommunityServiceType): CommunityServiceType
    fun insert(homeHealthTypee: List<CommunityServiceType>): List<CommunityServiceType> {
        return homeHealthTypee.map { insert(it) }
    }

    fun find(query: String): List<CommunityServiceType>
}

val homeHealthTypes: HomeHealthTypeDao by lazy { MongoHomeHealthTypeDao(DaoFactory.host, DaoFactory.port) }
