package ffc.airsync.api.services.analytic

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Person
import ffc.entity.healthcare.analyze.HealthAnalyzer

interface AnalyticDAO : Dao {
    fun insert(
        orgId: String,
        personId: String,
        healthAnalyzer: HealthAnalyzer
    ): HealthAnalyzer

    fun getByPersonId(orgId: String, personId: String): HealthAnalyzer

    fun getByHouseId(orgId: String, houseId: String): List<Person>

    fun query(orgId: String, query: String): List<Person>

    fun smartQuery(orgId: String, query: String): List<Person>

    fun deleteByPersonId(orgId: String, personId: String)

    fun removeByOrgId(orgId: String)

    fun insertBlock(
        orgId: String,
        block: Int,
        healthAnalyzer: Map<String, HealthAnalyzer>
    ): Map<String, HealthAnalyzer>

    fun confirmBlock(
        orgId: String,
        block: Int
    )

    fun unConfirmBlock(
        orgId: String,
        block: Int
    )

    fun getBlock(
        orgId: String,
        block: Int
    ): Map<String, HealthAnalyzer>
}

val analyzers: AnalyticDAO by lazy { MongoAnalyticDAO(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
