package ffc.airsync.api.services.notification

interface NotifactionDao {

    fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean)
    fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean)
    fun getFirebaseToken(orgId: String): List<String>
}

val notification: NotifactionDao by lazy { MongoFirebaseNotificationTokenDao() }
