package ffc.airsync.api.dao

import ffc.entity.Lang
import ffc.entity.healthcare.Disease

interface DiseaseDao : Dao {
    fun insert(disease: Disease): Disease
    fun insert(disease: List<Disease>): List<Disease>

    fun find(query: String): List<Disease>
    fun find(query: String, lang: Lang): List<Disease>
}