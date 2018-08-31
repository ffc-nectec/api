package ffc.airsync.api.dao

import ffc.entity.healthcare.CommunityServiceType

interface HomeHealthTypeDao : Dao {
    fun insert(homeHealthTypee: Map<String, String>): Map<String, String>
    fun insert(homeHealthTypee: List<Map<String, String>>): List<Map<String, String>>

    fun find(query: String): List<CommunityServiceType>
}