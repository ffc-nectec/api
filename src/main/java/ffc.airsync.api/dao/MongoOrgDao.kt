package ffc.airsync.api.dao

import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
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
import javax.ws.rs.NotFoundException

class MongoOrgDao(host: String, port: Int, databaseName: String, collection: String) : OrgDao, UserDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insert(organization: Organization): Organization {
        printDebug("Call mongo insert organization")
        `ตรวจสอบเงื่อนไขการลงทะเบียน Org`(organization)

        organization.users.forEach {
            it.password = getPass(it.password)
        }

        val genId = ObjectId()
        val orgDoc = Document.parse(ffcGson.toJson(organization))

        orgDoc.append("_id", genId)
        orgDoc["id"] = genId.toHexString()
        orgDoc.append("lastKnownIp", organization.bundle["lastKnownIp"])

        val genToken = ObjectId()
        orgDoc.append("token", genToken)

        dbCollection.insertOne(orgDoc)

        val query = Document("_id", genId)
        val newOrgDoc = dbCollection.find(query).first()

        return newOrgDoc.toJson().parseTo()
    }

    private fun `ตรวจสอบเงื่อนไขการลงทะเบียน Org`(organization: Organization) {
        if (organization.name.isEmpty()) throw BadRequestException("โปรระบุชื่อ หน่วยงานที่ต้องการลงทะเบียนลงในตัวแปร name")
        if (organization.users.isEmpty()) throw BadRequestException("โปรดลงทะเบียน user ในตัวแปร user ในหน่วยงานที่ต้องการลงทะเบียน")

        organization.users.forEach {
            @Suppress("DEPRECATION") if (it.username?.isNotEmpty() == true) throw BadRequestException("ตัวแปร username ยกเลิกการใช้งานแล้ว")
            if (it.name.isEmpty()) throw BadRequestException("พบค่าว่างในตัวแปร user.name")
            if (it.password.isEmpty()) throw BadRequestException("พบค่าว่างในตัวแปร user.password")
        }

        organization.users.find {
            it.role == User.Role.ORG
        } ?: throw BadRequestException("ไม่มี User ที่เป็น Role ORG")
        val query = Document("name", organization.name)
        val checkDuplicateName = dbCollection.find(query).first()
        if (checkDuplicateName != null) {
            throw BadRequestException("ลงทะเบียน Org ซ้ำ")
        }
        if (!organization.isTempId) throw BadRequestException("ไม่สามารถ Register ได้ โปรดตรวจสอบ id")
    }

    override fun remove(orgId: String) {
        printDebug("Call OrgMongoDao remove $orgId")
        val query = Document("id", orgId)
        dbCollection.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบ Org $orgId ที่ต้องการลบ")
    }

    override fun findAll(): List<Organization> {
        printDebug("Mongo findAll() org")
        val orgCursorList = dbCollection.find()
        val orgList = docListToObj(orgCursorList)
        if (orgList.isEmpty()) throw NotFoundException("ไม่พบรายการ org ลงทะเบียน ในระบบ")
        return orgList
    }

    private fun docListToObj(list: FindIterable<Document>): List<Organization> {
        val orgList = arrayListOf<Organization>()
        printDebug("\t\tLoad doc list.")
        list.forEach {
            printDebug("\t\t$it")
            val organization: Organization = it.toJson().parseTo()
            orgList.add(organization)
        }
        printDebug("\tReturn docListToObj")
        return orgList
    }

    override fun find(orgId: String): Organization {
        printDebug("Find by orgId = $orgId")
        val query = Document("id", orgId)
        val orgDocument = dbCollection.find(query).first()
        printDebug("\t$orgDocument")
        return docToObj(orgDocument)
    }

    private fun docToObj(orgDocument: Document): Organization = orgDocument.toString().parseTo()

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        printDebug("Mongo findAll org ip $ipAddress")
        val query = Document("lastKnownIp", ipAddress)
        printDebug("\tCreate query object $query")
        val orgDoc = dbCollection.find(query)
        printDebug("\tQuery org from mongo $orgDoc")
        val orgList = docListToObj(orgDoc)
        if (orgList.isEmpty()) throw NotFoundException("ไม่พบรายการลงทะเบียนในกลุ่มของ Org ip $ipAddress")
        return orgList
    }

    override fun findByToken(token: String): Organization {
        printDebug("Mongo findAll org token $token")
        val query = Document("token", token)
        val doc = dbCollection.find(query).first() ?: throw NotFoundException("ไม่พบ token $token ที่ค้นหา")
        return docToObj(doc)
    }

    override fun updateToken(organization: Organization): Organization {
        printDebug("Mongo update token orgobj=${organization.toJson()}")
        val query = Document("id", organization.id)
        dbCollection.find(query).first()
                ?: throw NotFoundException("ไม่พบ Object organization ${organization.id} ให้ Update")

        val generateToken = ObjectId()
        printDebug("\tGenerate Token ${generateToken.toHexString()}")
        val tokenDocument = Document("token", generateToken)
        val queryUpdate = Document("\$set", tokenDocument)

        dbCollection.updateOne(query, queryUpdate)
        printDebug("\tUpdate token.")

        val newOrgDocument = dbCollection.find(query).first()
        val newOrg = docToObj(newOrgDocument)

        printDebug("\tReturn updateToken")
        return newOrg
    }

    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = Document("id", orgId)
        if (isOrg) {
            val firebaseTokenDoc = Document("firebaseToken", firebaseToken)
            dbCollection.updateOne(query, BasicDBObject("\$set", firebaseTokenDoc))
        } else {
            dbCollection.updateOne(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }
    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {

        val query = Document("id", orgId)

        if (isOrg) {
            val removeOrgFirebaseToken = Document("firebaseToken", null)
            val removeOrgFirebaseTokenQuery = Document("\$set", removeOrgFirebaseToken)
            dbCollection.updateOne(query, removeOrgFirebaseTokenQuery)
        } else {
            val removeMobileFirebaseToken = Document("mobileFirebaseToken", firebaseToken)
            val removeMobileFirebaseTokenQuery = Document("\$pull", removeMobileFirebaseToken)
            dbCollection.updateOne(query, removeMobileFirebaseTokenQuery)
        }
    }

    override fun getFirebaseToken(orgId: String): List<String> {
        val firebaseTokenList = arrayListOf<String>()
        val query = Document("id", orgId)
        val firebaseOrgDoc = dbCollection.find(query).projection(Document("firebaseToken", 1)).projection(Document("mobileFirebaseToken", 1)).first()
        val firebaseMobile = firebaseOrgDoc["mobileFirebaseToken"] as List<*>

        firebaseTokenList.add(firebaseOrgDoc["firebaseToken"].toString())
        firebaseMobile.forEach {
            firebaseTokenList.add(it.toString())
        }
        return firebaseTokenList
    }

    override fun insertUser(user: User, orgId: String) {
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
    }

    private fun haveUserInDb(orgId: String, user: User): Boolean {
        printDebug("\t\t\tCall haveUserInDb")
        val query = Document("id", orgId)
        val userInDb = dbCollection.find(query).projection(Document("users", 1)).first()
        printDebug("\t\t\tOrg in haveUserInDb = $userInDb")
        val userList: Array<User> = userInDb["users"]!!.toJson().parseTo()
        printDebug("\t\t\tUser obj = ${userList.toJson()}")
        val userDuplicate = userList.find {
            it.name == user.name
        }
        return (userDuplicate != null)
    }

    override fun updateUser(user: User, orgId: String) {
        if (!haveUserInDb(orgId, user)) throw NotFoundException("ไม่พบผู้ใช้ ${user.name} ในระบบ")

        // TODO("รอพัฒนาระบบ Update User")
        // val query = Document("_id", ObjectId(orgId)).append("users", Document("name", user.name))
    }

    override fun findUser(orgId: String): List<User> {
        printDebug("Find User in orgId $orgId")
        val query = Document("_id", ObjectId(orgId))
        val userDocList = dbCollection.find(query).projection(Document("users", 1)).first()

        @Suppress("UNCHECKED_CAST") val userList: List<User> = userDocList.get("users", List::class.java) as List<User>

        userList.forEach {
            printDebug("\t${it.name}")
        }
        return userList
    }

    override fun getUser(name: String, pass: String, orgId: String): User? {
        printDebug("Call getUser in OrgMongoDao")

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
