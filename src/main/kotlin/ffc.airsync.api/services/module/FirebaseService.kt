package ffc.airsync.api.services.module

object FirebaseService {
    fun createOrgToken(orgId: String, firebaseToken: String) {
        orgs.createFirebase(orgId, firebaseToken, true)
    }

    fun createMobileToken(orgId: String, firebaseToken: String) {
        orgs.createFirebase(orgId, firebaseToken, false)
    }
}
