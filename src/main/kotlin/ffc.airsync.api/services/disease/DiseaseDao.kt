package ffc.airsync.api.services.disease

import ffc.airsync.api.services.Dao
import ffc.entity.Lang
import ffc.entity.healthcare.Disease
import ffc.entity.healthcare.Icd10

interface DiseaseDao : Dao {
    fun insert(disease: Icd10): Icd10

    fun insert(disease: List<Icd10>): List<Icd10> = disease.map { insert(it) }

    fun getByIcd10(icd10: String): Icd10?

    fun find(query: String = "", lang: Lang = Lang.th): List<Icd10>
}

val diseases: DiseaseDao by lazy { MongoDiseaseDao() }

/**
 * Lookup disease by icd10
 */
fun String.getIcd10() = diseases.getByIcd10(this)

/**
 * Lookup disease by icd10 field.
 * @return Disease id, name, icd10
 */
fun Icd10.getIcd10() = this.icd10.let { diseases.getByIcd10(it) }

fun Disease.findIcd10() = this.name.let { diseases.find(it).first() }

fun List<Icd10>.getIcd10() = this.map { it.getIcd10() ?: it }

fun List<Disease>.findIcd10() = this.map { it.findIcd10() }
