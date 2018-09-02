package ffc.airsync.api.services.module

object FirebaseService {
    fun createOrgToken(orgId: String, firebaseToken: String) {
        orgDao.createFirebase(orgId, firebaseToken, true)
    }

    fun createMobileToken(orgId: String, firebaseToken: String) {
        orgDao.createFirebase(orgId, firebaseToken, false)
    }
}
