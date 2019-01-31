package ffc.airsync.api.services.specialpp

import ffc.airsync.api.services.Dao
import ffc.entity.healthcare.SpecialPP.PPType

interface SpecialPpDao : Dao {
    fun insert(ppType: PPType)
    fun insert(ppType: List<PPType>) = ppType.forEach { insert(it) }

    fun get(id: String): PPType

    fun query(query: String): List<PPType>
}

val specialPPs: SpecialPpDao by lazy { MongoSpecialPpType() }
