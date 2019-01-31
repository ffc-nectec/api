package ffc.airsync.api.services.notification

import ffc.airsync.api.services.Dao
import ffc.entity.Entity

interface FirebaseStatusDao : Dao {
    fun insert(orgId: String, entityId: String)
    fun confirmSuccess(orgId: String, entityId: String)

    override fun syncData(orgId: String, limitOutput: Int): List<Entity>
}

val firebaseStauts: FirebaseStatusDao by lazy { MongoFirebaseStatusDao() }
