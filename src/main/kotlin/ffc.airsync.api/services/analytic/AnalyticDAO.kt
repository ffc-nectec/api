package ffc.airsync.api.services.analytic

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.healthcare.analyze.HealthAnalyzer

interface AnalyticDAO : Dao {
    fun insert(
        orgId: String,
        personId: String,
        houseId: String,
        healthAnalyzer: HealthAnalyzer
    ): HealthAnalyzer

    fun getByPersonId(orgId: String, personId: String): HealthAnalyzer

    fun getByHouseId(orgId: String, houseId: String): List<HealthAnalyzer>

    fun query(orgId: String, query: String): HashMap<String, HealthAnalyzer>

    fun deleteByPersonId(orgId: String, personId: String)

    fun deleteByOrgId(orgId: String)
}

val analyzers: AnalyticDAO by lazy { MongoAnalyticDAO(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
