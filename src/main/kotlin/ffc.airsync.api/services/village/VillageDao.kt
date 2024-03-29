package ffc.airsync.api.services.village

import ffc.airsync.api.services.Dao
import ffc.entity.Village

interface VillageDao : Dao {
    fun insert(orgId: String, village: Village): Village
    fun insert(orgId: String, village: List<Village>): List<Village> {
        return village.map { insert(orgId, it) }
    }

    fun update(orgId: String, village: Village): Village

    fun delete(orgId: String, id: String)

    fun get(orgId: String, id: String): Village

    fun find(orgId: String, query: String): List<Village>

    fun find(orgId: String): List<Village>

    fun removeByOrgId(orgId: String)
}

val villages: VillageDao by lazy { MongoVillageDao() }
