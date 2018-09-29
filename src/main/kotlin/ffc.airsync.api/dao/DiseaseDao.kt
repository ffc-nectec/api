package ffc.airsync.api.dao

import ffc.entity.Lang
import ffc.entity.healthcare.Disease

interface DiseaseDao : Dao {

    fun insert(disease: Disease): Disease

    fun insert(disease: List<Disease>): List<Disease> = disease.map { insert(it) }

    fun find(query: String = "", lang: Lang = Lang.th): List<Disease>
}
