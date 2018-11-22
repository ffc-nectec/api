package ffc.airsync.api.services.specialpp

import ffc.airsync.api.getResourceAs
import ffc.entity.healthcare.SpecialPP

object SpecialPpService {
    fun init() {
        if (specialPPs.query("").count() < 5)
            specialPPs.insert(getResourceAs<List<SpecialPP.PPType>>("specialPP.json"))
    }
}
