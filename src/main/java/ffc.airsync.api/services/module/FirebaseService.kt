package ffc.airsync.api.services.module

import ffc.entity.firebase.FirebaseToken
import java.util.*

object FirebaseService {
    fun updateToken(token: UUID, orgId: String, firebaseToken: FirebaseToken) {

        try {
            //val mobile = getOrgByMobileToken(token = token, orgId = orgId)
            //printDebug("Update firebase token mobile $firebaseToken")
            //mobile.data.firebaseToken = firebaseToken.firebasetoken
            //tokenMobile.updateFirebaseToken(token, firebaseToken.firebasetoken)
            orgDao.createFirebase(orgId, firebaseToken.firebasetoken, false)


        } catch (ex: Exception) {
            //val org = getOrgByOrgToken(token, orgId)
            //printDebug("Update firebase token organization $firebaseToken")
            //org.firebaseToken = firebaseToken.firebasetoken
            ex.printStackTrace()

        }

    }
}
