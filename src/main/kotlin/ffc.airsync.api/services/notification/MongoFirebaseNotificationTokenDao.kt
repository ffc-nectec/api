package ffc.airsync.api.services.notification

import com.mongodb.BasicDBObject
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal

class MongoFirebaseNotificationTokenDao : MongoDao("ffc", "organ"), NotifactionDao {

    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = "id" equal orgId
        if (isOrg) {
            val firebaseTokenDoc = "firebaseToken" equal firebaseToken
            dbCollection.updateOne(query, BasicDBObject("\$set", firebaseTokenDoc))
        } else {
            dbCollection.updateOne(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }
    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = "id" equal orgId

        if (isOrg) {
            val removeOrgFirebaseToken = "firebaseToken" equal null
            val removeOrgFirebaseTokenQuery = "\$set" equal removeOrgFirebaseToken
            dbCollection.updateOne(query, removeOrgFirebaseTokenQuery)
        } else {
            val removeMobileFirebaseToken = "mobileFirebaseToken" equal firebaseToken
            val removeMobileFirebaseTokenQuery = "\$pull" equal removeMobileFirebaseToken
            dbCollection.updateOne(query, removeMobileFirebaseTokenQuery)
        }
    }

    override fun getFirebaseToken(orgId: String): List<String> {
        try {
            logger.debug("Get firebase token.")
            val firebaseTokenList = arrayListOf<String>()
            val firebaseOrgDoc = dbCollection.find("id" equal orgId).first()
            val firebaseMobile = firebaseOrgDoc["mobileFirebaseToken"] as List<*>?
            val orgFirebase = firebaseOrgDoc["firebaseToken"].toString()
            if (orgFirebase != "null")
                firebaseTokenList.add(orgFirebase)

            firebaseMobile?.forEach {
                if (it != "null") {
                    logger.trace("\t\t\t\t\t$it")
                    firebaseTokenList.add(it.toString())
                }
            }
            return firebaseTokenList
        } catch (ex: Exception) {
            logger.info(ex.message)
            throw ex
        }
    }

    companion object {
        private val logger = getLogger()
    }
}
