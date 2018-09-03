package ffc.airsync.api.dao

import ffc.entity.healthcare.CommunityServiceType

interface HomeHealthTypeDao : Dao {
    fun insert(homeHealthTypee: CommunityServiceType): CommunityServiceType
    fun insert(homeHealthTypee: List<CommunityServiceType>): List<CommunityServiceType>

    fun find(query: String): List<CommunityServiceType>
}
