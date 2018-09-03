package ffc.airsync.api.services.module

import ffc.airsync.api.getResourceAs
import ffc.entity.healthcare.Disease

object DiseaseService {

    fun query(query: String): List<Disease> {
        return diseaseDao.find(query)
    }

    fun init() {
        if (query("").count() < 5) {
            diseaseDao.insert(getResourceAs<List<Disease>>("Disease.json"))
        }
    }
}
