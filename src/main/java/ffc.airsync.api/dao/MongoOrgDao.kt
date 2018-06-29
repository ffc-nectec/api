package ffc.airsync.api.dao


import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import ffc.airsync.api.printDebug
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.ffcGson
import ffc.entity.parseTo
import ffc.entity.toJson
import org.bson.Document
import org.bson.types.ObjectId
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException

class MongoOrgDao(host: String, port: Int, databaseName: String, collection: String) : OrgDao, UserDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insert(organization: Organization): Organization {

        `ตรวจสอบความครบถ้วนของข้อมูล`(organization)
        if (!organization.isTempId) throw BadRequestException("ไม่สามารถ Register ได้ โปรดตรวจสอบ id")

        printDebug("Call mongo insert organization")
        val genId = ObjectId()
        val orgDoc = Document.parse(ffcGson.toJson(organization))

        orgDoc.append("_id", genId)
        orgDoc["id"] = genId.toHexString()
        orgDoc.append("lastKnownIp", organization.bundle["lastKnownIp"])

        val genToken = ObjectId()
        orgDoc.append("token", genToken)

        coll2.insertOne(orgDoc)


        val query = Document("_id", genId)
        val newOrgDoc = coll2.find(query).first()

        return newOrgDoc.toJson().parseTo()
    }

    private fun `ตรวจสอบความครบถ้วนของข้อมูล`(organization: Organization) {
        if (organization.name.isEmpty()) throw BadRequestException("โปรระบุชื่อ หน่วยงานที่ต้องการลงทะเบียนลงในตัวแปร name")
        if (organization.users.isEmpty()) throw BadRequestException("โปรดลงทะเบียน user ในตัวแปร user ในหน่วยงานที่ต้องการลงทะเบียน")

        organization.users.forEach {
            if (it.username?.isNotEmpty() == true) throw BadRequestException("ตัวแปร username ยกเลิกการใช้งานแล้ว")
            if (it.name.isEmpty()) throw BadRequestException("พบค่าว่างในตัวแปร user.name")
            if (it.password.isEmpty()) throw BadRequestException("พบค่าว่างในตัวแปร user.password")
        }

        val query = Document("name", organization.name)
        val checkDuplicateName = coll2.find(query).first()
        if (checkDuplicateName != null) {
            throw BadRequestException("ลงทะเบียน Org ซ้ำ")
        }

    }


    override fun remove(orgId: String) {
        val query = Document("_id", ObjectId(orgId))
        coll2.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบ Org $orgId ที่ต้องการลบ")
    }

    override fun findAll(): List<Organization> {
        printDebug("Mongo findAll() org")
        val orgCursorList = coll2.find()
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
        val query = Document("_id", ObjectId(orgId))
        val orgDocument = coll2.find(query).first()
        printDebug("\t$orgDocument")
        return docToObj(orgDocument)
    }

    private fun docToObj(orgDocument: Document): Organization = orgDocument.toString().parseTo()

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        printDebug("Mongo findAll org ip $ipAddress")
        val query = Document("lastKnownIp", ipAddress)
        printDebug("\tCreate query object $query")
        val orgDoc = coll2.find(query)
        printDebug("\tQuery org from mongo $orgDoc")
        val orgList = docListToObj(orgDoc)

        if (orgList.isEmpty()) throw NotFoundException("ไม่พบรายการลงทะเบียนในกลุ่มของ Org ip $ipAddress")


        return orgList
    }

    override fun findByToken(token: String): Organization {
        printDebug("Mongo findAll org token $token")
        val query = Document("token", token)
        val doc = coll2.find(query).first() ?: throw NotFoundException("ไม่พบ token $token ที่ค้นหา")

        return docToObj(doc)

    }

    override fun updateToken(organization: Organization): Organization {
        printDebug("Mongo update token orgobj=${organization.toJson()}")
        val query = Document("_id", ObjectId(organization.id))
        coll2.find(query).first()
                ?: throw NotFoundException("ไม่พบ Object organization ${organization.id} ให้ Update")


        val generateToken = ObjectId()
        printDebug("\tGenerate Token ${generateToken.toHexString()}")
        val tokenDocument = Document("token", generateToken)
        val queryUpdate = Document("\$set", tokenDocument)

        coll2.updateOne(query, queryUpdate)
        printDebug("\tUpdate token.")


        val newOrgDocument = coll2.find(query).first()
        val newOrg = docToObj(newOrgDocument)

        printDebug("\tReturn updateToken")
        return newOrg
    }


    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = Document("_id", ObjectId(orgId))
        if (isOrg) {
            val firebaseTokenDoc = Document("firebaseToken", firebaseToken)
            coll2.updateOne(query, BasicDBObject("\$set", firebaseTokenDoc))

        } else {
            coll2.updateOne(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }


    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {

        val query = Document("_id", ObjectId(orgId))

        if (isOrg) {
            val removeOrgFirebaseToken = Document("firebaseToken", null)
            val removeOrgFirebaseTokenQuery = Document("\$set", removeOrgFirebaseToken)
            coll2.updateOne(query, removeOrgFirebaseTokenQuery)
        } else {
            val removeMobileFirebaseToken = Document("mobileFirebaseToken", firebaseToken)
            val removeMobileFirebaseTokenQuery = Document("\$pull", removeMobileFirebaseToken)
            coll2.updateOne(query, removeMobileFirebaseTokenQuery)
        }

    }

    override fun getFirebaseToken(orgId: String): List<String> {
        val firebaseTokenList = arrayListOf<String>()

        val query = Document("_id", ObjectId(orgId))

        val firebaseOrgDoc = coll2.find(query)
                .projection(Document("firebaseToken", 1))
                .projection(Document("mobileFirebaseToken", 1)).first()

        val firebaseMobile = firebaseOrgDoc["mobileFirebaseToken"] as List<*>

        firebaseTokenList.add(firebaseOrgDoc["firebaseToken"].toString())
        firebaseMobile.forEach {
            firebaseTokenList.add(it.toString())
        }

        return firebaseTokenList
    }


    override fun insertUser(user: User, orgId: String) {

        val query = Document("_id", ObjectId(orgId))

        val userDoc = Document.parse(ffcGson.toJson(user))
        val userStruct = Document("users", userDoc)
        val userPush = Document("\$push", userStruct)

        if (haveUserInDb(orgId, user)) throw BadRequestException("มีการเพิ่มผู้ใช้ ${user.name} ซ้ำ")

        coll2.updateOne(query, userPush)


    }

    private fun haveUserInDb(orgId: String, user: User): Boolean {
        val query = Document("_id", ObjectId(orgId))
        val userInDb = coll2.find(query).projection(Document("users", 1)).first()
        val userList: Array<User> = userInDb.toJson().parseTo()
        val userDuplicate = userList.find {
            it.name == user.name
        }
        return (userDuplicate != null)
    }

    override fun updateUser(user: User, orgId: String) {
        if (!haveUserInDb(orgId, user)) throw NotFoundException("ไม่พบผู้ใช้ ${user.name} ในระบบ")


        //TODO("รอพัฒนาระบบ Update User")
        val query = Document("_id", ObjectId(orgId))
                .append("users", Document("name", user.name))

        val updateUser = Document("_id", ObjectId(orgId))
                .append("users", arrayListOf<User>().apply {
                    this.add(user)
                })


        val oldUserDoc = coll2.find(query).first()


        val updateDoc = Document("\$pull", oldUserDoc)

        //coll2.updateOne(query,replace)
    }

    override fun findUser(orgId: String): List<User> {
        printDebug("Find User in orgId $orgId")
        val query = Document("_id", ObjectId(orgId))
        val userDocList = coll2.find(query).projection(Document("users", 1)).first()

        val userList = userDocList.get("users", List::class.java) as List<User>
        userList.forEach {
            printDebug("\t${it.name}")
        }

        return userList
    }

    override fun isAllowUser(name: String, pass: String, orgId: String): Boolean {
        printDebug("Call isAllowUser in OrgMongoDao")

        val query = Document("_id", ObjectId(orgId))


        printDebug("\tQuery = ${query.toJson()}")
        val orgDoc = coll2.find(query).first()
        printDebug("\torgDoc = ${orgDoc.toJson()}")


        val org = orgDoc.toJson().parseTo<Organization>()
        printDebug("\torg = $org")

        val user = org.users.find {
            it.name == name && it.password == pass
        }
        //val user = coll2.find(query).first()
        printDebug("\tuser query $user")
        return user != null
    }
}
