package ffc.airsync.api.services.module

import ffc.entity.firebase.FirebaseToken
import java.util.UUID

object FirebaseService {
    fun updateToken(token: UUID, orgId: String, firebaseToken: FirebaseToken) {

        try {
            orgDao.createFirebase(orgId, firebaseToken.firebasetoken, false)


        } catch (ex: Exception) {
            //val org = getOrgByOrgToken(token, orgId)
            //printDebug("Update firebase token organization $firebaseToken")
            //org.firebaseToken = firebaseToken.firebasetoken
            ex.printStackTrace()

        }

    }
}
