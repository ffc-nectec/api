package ffc.airsync.api.services.module

import ffc.entity.firebase.FirebaseToken

object FirebaseService {
    fun createOrgToken(orgId: String, firebaseToken: FirebaseToken) {
        orgDao.createFirebase(orgId, firebaseToken.firebasetoken, true)
    }

    fun createMobileToken(orgId: String, firebaseToken: FirebaseToken) {
        orgDao.createFirebase(orgId, firebaseToken.firebasetoken, false)
    }
}
