package ffc.airsync.api.services.disease

import ffc.airsync.api.services.Dao
import ffc.airsync.api.services.DaoFactory
import ffc.entity.Lang
import ffc.entity.healthcare.Disease

interface DiseaseDao : Dao {
    fun insert(disease: Disease): Disease

    fun insert(disease: List<Disease>): List<Disease> = disease.map { insert(it) }

    fun find(query: String = "", lang: Lang = Lang.th): List<Disease>
}

val diseases: DiseaseDao by lazy { MongoDiseaseDao(DaoFactory.host, DaoFactory.port) }
