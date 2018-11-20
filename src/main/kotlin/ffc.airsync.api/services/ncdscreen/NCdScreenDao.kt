package ffc.airsync.api.services.ncdscreen

import ffc.airsync.api.services.Dao
import ffc.airsync.api.services.SyncDao
import ffc.entity.healthcare.NCDScreen

interface NCdScreenDao : SyncDao<NCDScreen>, Dao {
    fun insert(orgId: String, ncdScreen: NCDScreen): NCDScreen

    fun removeOrg(orgId: String)
}
