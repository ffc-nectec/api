package ffc.airsync.api.dao

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import ffc.airsync.api.dao.UserDao.Companion.checkBlockUser
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.UserStor
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*


class MongoUserDao(host: String, port: Int, databaseName: String, collection: String) : UserDao, MongoAbsConnect(host, port, databaseName, collection) {

    private val SALTPASS = """
uxF3Ocv5eg4BoQBK9MmR
rwPARiCL9ovpr3zmlJlj
kIQnpzRIgEh8WLFNHyy1
ALqs9ES1aQlsc47DlG5f
SbAOMWzMd1T03dyigoHR
7hox2nDJ7tMJRHab5gsy
Ux2VxiCIvJtfPAobOxYW
HazJzQEGdXpmeM2aK6MD
mpOARM2427A6CY14uomK
Cxe9aEkJEFtlLLo6NaNW
yLkbHUfMNDwWeu2BRXuS
m7BHwYSyKGFJdLnq4jJd
sr4QI6aK7g3GCm8vG6Pd
RAtlJZFto0bi9OZta5b4
DLrNTZXXtB3Ci17sepXU
HSYUuw11GJmeuiLKgJYZ
PCHuw2hpoozErKVxEv86
f6zMttthJyQnrDBHGhma
j1nrasD5fg9NxuwkdJq8
ytF2v69RwtGYf7C6ygwD
"""


    override fun insert(user: User, org: Organization) {
        //printDebug("Insert username mongo. ${user.toJson()}")


        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val query = BasicDBObject("user", user.username)

                coll.remove(query)
            }
        })


        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val userDoc = objToDoc(user, org)
                coll.insert(userDoc)
            }
        })


    }

    override fun find(orgUuid: UUID): List<UserStor> {
        val listUser = arrayListOf<UserStor>()

        val query = BasicDBObject("orgUuid", orgUuid.toString())

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val userListDoc = coll.find(query)

                if (userListDoc.hasNext()) {
                    val userDoc = userListDoc.next()
                    val userStor = docToUserObj(userDoc)
                    listUser.add(userStor)
                }
            }
        })




        return listUser
    }


    override fun findById(id: String): List<UserStor> {
        val listUser = arrayListOf<UserStor>()

        val query = BasicDBObject("orgId", id)


        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val userListDoc = coll.find(query)

                if (userListDoc.hasNext()) {
                    val userDoc = userListDoc.next()
                    val userStor = docToUserObj(userDoc)
                    listUser.add(userStor)
                }
            }
        })




        return listUser


    }


    override fun isAllow(user: User, orgUuid: UUID): Boolean {
        checkBlockUser(user)

        var userDoc: DBObject? = null

        val query = BasicDBObject("orgUuid", orgUuid.toString())
                .append("user", user.username)
                .append("pass", getPass(user.password))

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                userDoc = coll.findOne(query)
            }
        })


        return userDoc != null
    }

    override fun isAllowById(user: User, orgId: String): Boolean {
        checkBlockUser(user)
        val query = BasicDBObject("orgId", orgId)
                .append("user", user.username)
                .append("pass", getPass(user.password))


        var userDoc: DBObject? = null

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                userDoc = coll.findOne(query)
            }
        })

        return userDoc != null
    }

    override fun removeByOrgUuid(orgUUID: UUID) {

        val query = BasicDBObject("orgUuid", orgUUID.toString())

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                coll.remove(query)
            }
        })


    }

    private fun docToUserObj(userDoc: DBObject): UserStor {
        val username = userDoc.get("user").toString()
        val password = userDoc.get("pass").toString()
        val orgId = userDoc.get("orgId").toString()
        val orgUuid = UUID.fromString(userDoc.get("orgUuid").toString())
        val user = User(username = username, password = password)

        return UserStor(user = user, orgUuid = orgUuid, orgId = orgId)
    }

    private fun getPass(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedhash = digest.digest(
                (SALTPASS + password).toByteArray(StandardCharsets.UTF_8))

        val hexString = StringBuffer()
        for (i in 0 until encodedhash.size) {
            val hex = Integer.toHexString(0xff and encodedhash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun objToDoc(user: User, org: Organization): BasicDBObject {
        val userDoc = BasicDBObject("orgUuid", org.uuid.toString())
                .append("orgId", org.id)
                .append("user", user.username)
                .append("pass", getPass(user.password))
        return userDoc
    }
}
