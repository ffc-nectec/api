package ffc.airsync.api.dao

import ffc.airsync.api.dao.PasswordSalt.getPass
import ffc.airsync.api.dao.UserDao.Companion.checkBlockUser
import ffc.airsync.api.printDebug
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import javax.ws.rs.NotFoundException

class MongoUserDao(host: String, port: Int, databaseName: String, collection: String) : UserDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insertUser(user: User, orgId: String) {

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val userDoc = Document.parse(user.toJson())
                userDoc.append("orgId", orgId)
                coll2.insertOne(userDoc)
            }
        })
    }

    override fun updateUser(user: User, orgId: String) {
        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val query = Document.parse(user.toJson()).append("orgId", orgId)
                query["password"] = null

                val userDoc = Document.parse(user.toJson())
                userDoc.append("orgId", orgId)

                coll2.findOneAndReplace(query, userDoc) ?: throw NotFoundException("ไม่พบ User ${user.name} ให้ Update")
            }
        })
    }

    override fun findUser(orgId: String): List<User> {
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

    override fun getUser(name: String, pass: String, orgId: String): User? {
        checkBlockUser(name)

        var userDoc: Document? = null
        val query = Document("orgId", orgId).append("name", name).append("pass", getPass(pass))

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                userDoc = coll2.find(query).first()
                printDebug("\tQuery user in mongo $userDoc")
            }
        })

        return userDoc?.toJson()?.parseTo()
    }
}
