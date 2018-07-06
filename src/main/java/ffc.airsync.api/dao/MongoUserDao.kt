package ffc.airsync.api.dao

import com.google.gson.Gson
import ffc.airsync.api.dao.PasswordSalt.getPass
import ffc.airsync.api.printDebug
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.ffcGson
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId
import javax.ws.rs.BadRequestException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.NotFoundException

class MongoUserDao(host: String, port: Int, databaseName: String, collection: String)
    : UserDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insertUser(user: User, orgId: String): User {
        printDebug("Call MongoOrd insert User ${user.toJson()}")
        if (!user.isTempId) throw BadRequestException("รุปแบบ id ต้องใช้ TempId ในการสร้าง User")

        val generateId = ObjectId()
        val userInsert = user.copy<User>(generateId.toHexString())
        printDebug("\tCreate new user object")

        printDebug("\tCheck user dupp.")
        if (haveUserInDb(orgId, user)) throw BadRequestException("มีการเพิ่มผู้ใช้ ${userInsert.name} ซ้ำ")

        val query = Document("id", orgId)
        userInsert.password = getPass(userInsert.password)
        val userDoc = Document.parse(userInsert.toJson())
        val userStruct = Document("users", userDoc)
        val userPush = Document("\$push", userStruct)

        printDebug("\tCreate user in mongo")
        dbCollection.updateOne(query, userPush)

        findUser(orgId).forEach {
            println("\t\tuser $it")
        }
        printDebug("\tcall regis user new update.")

        return findUser(orgId).find {
            it.name == user.name
        } ?: throw InternalServerErrorException("Server Error in call dev")
    }

    private fun haveUserInDb(orgId: String, user: User): Boolean {
        printDebug("\t\t\tCall haveUserInDb")
        val query = Document("id", orgId)
        val userInDb = dbCollection.find(query).projection(Document("users", 1)).first()
        printDebug("\t\t\tOrg in haveUserInDb = $userInDb")
        val userList: Array<User>? = userInDb?.get("users")?.toJson()?.parseTo()
        printDebug("\t\t\tUser obj = ${userList?.toJson()}")
        val userDuplicate = userList?.find {
            it.name == user.name
        }
        return (userDuplicate != null)
    }

    override fun updateUser(user: User, orgId: String): User {
        if (!haveUserInDb(orgId, user)) throw NotFoundException("ไม่พบผู้ใช้ ${user.name} ในระบบ")

        // TODO("รอพัฒนาระบบ Update User")
        // val query = Document("_id", ObjectId(orgId)).append("users", Document("name", user.name))
        return User().apply {
            name = "Dymmy"
            password = "Dymmy"
        }
    }

    override fun findUser(orgId: String): List<User> {
        printDebug("Find User in orgId $orgId")
        val query = Document("_id", ObjectId(orgId))
        val userDocList = dbCollection.find(query).projection(Document("users", 1)).first() ?: return arrayListOf()
        printDebug("\tuser list ${userDocList.toJson()}")
        return userDocList.getAs("users") ?: listOf()
    }

    inline fun <reified T> Document.getAs(key: String, gson: Gson = ffcGson): T? {
        return this.get(key)?.toJson(gson)?.parseTo()
    }

    override fun getUser(name: String, pass: String, orgId: String): User? {
        printDebug("Call getUser in OrgMongoDao")
        if (UserDao.isBlockUser(name)) return null
        val query = Document("id", orgId)
        printDebug("\tQuery = ${query.toJson()}")
        val orgDoc = dbCollection.find(query).first() ?: throw NotFoundException("ไม่พบ Org id $orgId")
        printDebug("\torgDoc = ${orgDoc.toJson()}")
        val org = orgDoc.toJson().parseTo<Organization>()
        printDebug("\torg = $org")

        val passwordSalt = getPass(pass)
        printDebug("Salt Pass = $passwordSalt")

        val user = org.users.find {
            printDebug("\t\tUser= ${it.toJson()}")
            it.name == name && it.password == passwordSalt
        }
        // val user = dbCollection.find(query).first()
        printDebug("\tuser query $user")
        return user
    }
}
