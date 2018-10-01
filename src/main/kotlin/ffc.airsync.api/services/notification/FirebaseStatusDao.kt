package ffc.airsync.api.services.notification

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Entity

interface FirebaseStatusDao : Dao {
    fun insert(orgId: String, entityId: String)
    fun confirmSuccess(orgId: String, entityId: String)

    override fun syncCloudFilter(orgId: String, isSync: Boolean, limitOutput: Int): List<Entity>
}

val firebaseStauts: FirebaseStatusDao by lazy { MongoFirebaseStatusDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
