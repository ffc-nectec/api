package ffc.airsync.api.services.village

import ffc.airsync.api.services.Dao

interface VillageDao : Dao {
    fun insert(orgId: String, village: Village): Village
    fun insert(orgId: String, village: List<Village>): List<Village> {
        return village.map { insert(orgId, it) }
    }

    fun update(orgId: String, village: Village): Village

    fun get(id: String): Village

    fun find(orgId: String, query: String): List<Village>
}
