package ffc.airsync.api.services.village

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao

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
}

val villages: VillageDao by lazy { MongoVillageDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
