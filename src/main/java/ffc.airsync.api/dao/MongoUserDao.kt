package ffc.airsync.api.dao

import ffc.airsync.api.dao.UserDao.Companion.checkBlockUser
import ffc.entity.User
import ffc.entity.parseTo
import ffc.entity.toJson
import org.bson.Document
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.ws.rs.NotFoundException


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


    override fun insert(user: User, orgId: String) {


        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val userDoc = Document.parse(user.toJson())
                userDoc.append("orgId", orgId)
                coll2.insertOne(userDoc)
            }
        })
    }


    override fun update(user: User, orgId: String) {
        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val query = Document.parse(user.toJson())
                        .append("orgId", orgId)
                query["password"] = null

                val userDoc = Document.parse(user.toJson())
                userDoc.append("orgId", orgId)

                coll2.findOneAndReplace(query, userDoc) ?: throw NotFoundException("ไม่พบ User ${user.name} ให้ Update")
            }
        })


    }

    override fun find(orgId: String): List<User> {
        val listUser = arrayListOf<User>()

        val query = Document("orgId", orgId)

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val userListDoc = coll2.find(query)

                userListDoc.forEach {
                    val userDoc = it
                    val user: User = userDoc.toJson().parseTo()
                    listUser.add(user)
                }
            }
        })




        return listUser
    }


    override fun isAllow(user: User, orgId: String): Boolean {
        checkBlockUser(user)

        var userDoc: Document? = null

        val query = Document("orgId", orgId)
                .append("name", user.name)
                .append("pass", getPass(user.password))

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                userDoc = coll2.find(query).first()
            }
        })

        return userDoc != null
    }

    override fun removeByOrgId(orgId: String) {

        val query = Document("orgId", orgId)

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                coll2.deleteMany(query)
            }
        })
    }


    private fun getPass(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encoded = digest.digest(
                ("$password$SALTPASS$password").toByteArray(StandardCharsets.UTF_8))

        val hexString = StringBuffer()
        for (i in 0 until encoded.size) {
            val hex = Integer.toHexString(0xff and encoded[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }


}
