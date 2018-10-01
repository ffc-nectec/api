package ffc.airsync.api.services.disease

import ffc.airsync.api.getResourceAs
import ffc.entity.healthcare.Disease

object DiseaseService {

    fun init() {
        if (diseases.find().count() < 5) {
            diseases.insert(getResourceAs<List<Disease>>("Disease.json"))
        }
    }
}
