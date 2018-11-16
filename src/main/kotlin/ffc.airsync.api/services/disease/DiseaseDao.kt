package ffc.airsync.api.services.disease

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Lang
import ffc.entity.healthcare.Disease

interface DiseaseDao : Dao {
    fun insert(disease: Disease): Disease

    fun insert(disease: List<Disease>): List<Disease> = disease.map { insert(it) }

    fun getByIcd10(icd10: String): Disease?

    fun find(query: String = "", lang: Lang = Lang.th): List<Disease>
}

val diseases: DiseaseDao by lazy { MongoDiseaseDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }

/**
 * Lookup disease by icd10
 */
fun String.getDisease() = diseases.getByIcd10(this)

/**
 * Lookup disease by icd10 field.
 * @return Disease id, name, icd10
 */
fun Disease.getDisease() = this.icd10?.let { diseases.getByIcd10(it) }

fun List<Disease>.getDisease() = this.map { it.getDisease() ?: it }
