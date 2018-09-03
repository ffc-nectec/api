package ffc.airsync.api.services.module

import ffc.airsync.api.getResourceAs
import ffc.entity.Lang
import ffc.entity.healthcare.Disease

object DiseaseService {

    fun query(query: String, lang: Lang): List<Disease> {
        return diseaseDao.find(query, lang)
    }

    fun init() {
        if (diseaseDao.find("").count() < 5) {
            diseaseDao.insert(getResourceAs<List<Disease>>("Disease.json"))
        }
    }
}
