package ffc.airsync.api.services.notification

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT

interface NotifactionDao {

    fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean)
    fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean)
    fun getFirebaseToken(orgId: String): List<String>
}

val notification: NotifactionDao by lazy { MongoFirebaseNotificationTokenDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
