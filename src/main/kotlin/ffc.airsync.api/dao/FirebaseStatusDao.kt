package ffc.airsync.api.dao

import ffc.entity.Entity

interface FirebaseStatusDao : Dao {
    fun insert(orgId: String, entityId: String)
    fun confirmSuccess(orgId: String, entityId: String)

    override fun syncCloudFilter(orgId: String, isSync: Boolean, limitOutput: Int): List<Entity>
}
