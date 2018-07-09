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

class MongoOrgDao(host: String, port: Int) : OrgDao, MongoAbsConnect(host, port, "ffc", "organ") {

    override fun insert(organization: Organization): Organization {
        printDebug("Call mongo insert organization")
        `ตรวจสอบเงื่อนไขการลงทะเบียน Org`(organization)

        val userListDoc = arrayListOf<Document>()
        organization.users.forEach {
            if (it.isTempId) {
                val user = it.copy<User>(ObjectId().toHexString())
                user.password = getPass(user.password)
                printDebug("\t\tNew user create = ${user.toJson()}")

                val userDoc = Document.parse(user.toJson())
                userDoc.append("password", user.password)
                userListDoc.add(userDoc)
            } else throw BadRequestException("ข้อมูลที่จะสร้างใหม่จำเป็นต้องใช้ TempId")
        }

        val genId = ObjectId()
        val orgDoc = Document.parse(ffcGson.toJson(organization))

        orgDoc.append("users", userListDoc)

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

    override fun findById(orgId: String): Organization {
        printDebug("Find by orgId = $orgId")
        val query = Document("id", orgId)
        val orgDocument = dbCollection.find(query).first()
        printDebug("\torgDoc ${orgDocument.toJson()}")
        return docToObj(orgDocument)
    }

    private fun docToObj(orgDocument: Document): Organization = orgDocument.toJson().parseTo()

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
        try {
            printDebug("Get firebase token.")
            val firebaseTokenList = arrayListOf<String>()
            val query = Document("id", orgId)
            val firebaseOrgDoc = dbCollection.find(query).first()
            val firebaseMobile = firebaseOrgDoc["mobileFirebaseToken"] as List<*>?

            val orgFirebase = firebaseOrgDoc["firebaseToken"].toString()
            if (orgFirebase != "null")
                firebaseTokenList.add(orgFirebase)

            firebaseMobile?.forEach {
                if (it != "null") {
                    printDebug("\t\t\t\t\t$it")
                    firebaseTokenList.add(it.toString())
                }
            }
            return firebaseTokenList
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }
}
