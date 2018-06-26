package ffc.airsync.api.dao


import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import ffc.airsync.api.printDebug
import ffc.entity.Organization
import ffc.entity.ffcGson
import ffc.entity.parseTo
import ffc.entity.toJson
import org.bson.Document
import org.bson.types.ObjectId
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException

class MongoOrgDao(host: String, port: Int, databaseName: String, collection: String) : OrgDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insert(organization: Organization): Organization {

        printDebug("Call mongo insert organization")
        val generateId = ObjectId()

        if (!organization.isTempId) {
            throw BadRequestException("ไม่สามารถ Register ได้ โปรดตรวจสอบ id")
        }

        val query = Document("name", organization.name)
        val checkDuplicateName = coll2.find(query).first()
        if (checkDuplicateName != null) {
            throw BadRequestException("ไม่สามารถ Register ได้มีการใช้ชื่อ ${organization.name} ไปแล้ว")
        }

        val insertObject = Document.parse(ffcGson.toJson(organization))


        insertObject.append("_id", generateId)
        insertObject.append("lastKnownIp", organization.bundle["lastKnownIp"])
        val generateToken = ObjectId()
        insertObject.append("token", generateToken)

        coll2.insertOne(insertObject)


        val queryNewOrg = Document("_id", generateId)
        val newOrgDocument = coll2.find(queryNewOrg).first()

        return newOrgDocument.toJson().parseTo()
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
        val query = Document("_id", organization.id)
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
        val query = Document("idOrg", orgId)
        if (isOrg) {
            val firebaseTokenDoc = Document("firebaseToken", firebaseToken)
            coll2.updateOne(query, BasicDBObject("\$set", firebaseTokenDoc))

        } else {
            coll2.updateOne(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }


    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {

        val query = Document("orgId", orgId)

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
}
