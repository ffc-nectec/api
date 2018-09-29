package ffc.airsync.api.services.module

import ffc.airsync.api.getResourceAs
import ffc.entity.Lang
import ffc.entity.healthcare.Disease

object DiseaseService {

    fun init() {
        if (diseases.find().count() < 5) {
            diseases.insert(getResourceAs<List<Disease>>("Disease.json"))
        }
    }

    fun query(query: String, lang: Lang): List<Disease> {
        return diseases.find(query, lang)
    }
}
