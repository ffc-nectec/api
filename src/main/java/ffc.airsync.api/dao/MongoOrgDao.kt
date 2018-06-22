package ffc.airsync.api.dao


import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import ffc.airsync.api.get6DigiId
import ffc.airsync.api.printDebug
import ffc.entity.Organization
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*
import javax.ws.rs.NotFoundException

class MongoOrgDao(host: String, port: Int, databaseName: String, collection: String) : OrgDao, MongoAbsConnect(host, port, databaseName, collection) {

    companion object {
        private val COUNTERNAME = "idorg"

    }

    private val couterColl: MongoCollection<Document>


    init {


        try {
            if (mongoUrl.isEmpty() || mongoUrl.startsWith("null")) {
                printDebug("\tCall create counter by object.")
                couterColl = getClient()!!.getDatabase(dbName).getCollection("counter")
                printDebug("\t\tFinish call create counter.")
            } else {
                printDebug("\tCall create counter by url string parameter.")
                val databaseName = System.getenv("MONGODB_DBNAME")
                couterColl = getClient()!!.getDatabase(databaseName).getCollection("counter")
                printDebug("\t\tFinish call create counter.")
            }


            val counterDoc = Document("_id", COUNTERNAME)
                    .append("sec", 1)

            try {
                couterColl.insertOne(counterDoc)
            } catch (ex: com.mongodb.DuplicateKeyException) {
                printDebug("Org Counter Duplicate.")
            } catch (ex: com.mongodb.MongoWriteException) {
                printDebug("Org Counter Duplicate.")
                //ex.printStackTrace()
            }

            printDebug("\t\tInsert counter object.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            val exout = javax.ws.rs.InternalServerErrorException("Get collection org counter.")
            exout.stackTrace = ex.stackTrace
            throw exout
        }

    }


    override fun insert(organization: Organization) {

        printDebug("Call mongo insert organization")
        //val queryRemove = Document("orgUuid", organization.uuid.toString())
        //coll2.deleteOne(queryRemove)

        var index = ""

        try {
            val queryIndex = Document("_id", COUNTERNAME)
            val updateIndex = Document("\$inc",
                    Document("sec", 1))

            val newIndex = couterColl.findOneAndUpdate(queryIndex, updateIndex)
            index = newIndex["sec"].toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }

        printDebug("Counter counter $index")



        organization.id = index

        //uuid id token ipaddress
        val doc = createDoc(organization, ObjectId())
        //coll.insert(doc)
        coll2.insertOne(doc)


    }

    override fun find(): List<Organization> {
        printDebug("Mongo find() org")
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
            val organization = docToObj(it)
            orgList.add(organization)
        }
        printDebug("\tReturn docListToObj")
        return orgList

    }

    override fun findByUuid(uuid: UUID): Organization {
        printDebug("Mongo find org uuid $uuid")
        val query = Document("orgUuid", uuid.toString())
        val doc = coll2.find(query).first() ?: throw NotFoundException("ไม่พบ uuid ${uuid.toString()} ที่ค้นหา")
        val organization = docToObj(doc)

        return organization
    }

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        printDebug("Mongo find org ip $ipAddress")
        val query = Document("lastKnownIp", ipAddress)
        printDebug("\tCreate query object $query")
        val orgDoc = coll2.find(query)
        printDebug("\tQuery org from mongo $orgDoc")
        val orgList = docListToObj(orgDoc)

        if (orgList.isEmpty()) throw NotFoundException("ไม่พบรายการลงทะเบียนในกลุ่มของ Org ip $ipAddress")

        return orgList
    }

    override fun findByToken(token: UUID): Organization {
        printDebug("Mongo find org token $token")
        val query = Document("token", token.toString())
        val doc = coll2.find(query).first() ?: throw NotFoundException("ไม่พบ token ${token.toString()} ที่ค้นหา")
        val organization = docToObj(doc)

        return organization

    }

    override fun findById(id: String): Organization {
        printDebug("Mongo find org id $id")
        val query = Document("idOrg", id)
        val doc = coll2.find(query).first() ?: throw NotFoundException("ไม่พบ id org $id ที่ค้นหา")
        val organization = docToObj(doc)

        return organization
    }

    override fun updateToken(organization: Organization): Organization {
        printDebug("Mongo update token orgobj=$organization")
        val query = Document("orgUuid", organization.uuid.toString())
        val oldDoc = coll2.find(query).first()
                ?: throw NotFoundException("ไม่พบ Object organization ${organization.uuid} ให้ Update")


        organization.token = UUID.randomUUID()
        val updateDoc = createDoc(organization, ObjectId(oldDoc["_id"].toString()))

        coll2.updateOne(oldDoc, updateDoc)

        return organization
    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        printDebug("Mongo remove org uuid $orgUUID")
        val query = Document("orgUuid", orgUUID.toString())

        val doc = coll2.findOneAndDelete(query) ?: throw NotFoundException()
        coll2.deleteMany(query)

    }

    private fun createDoc(organization: Organization, objId: ObjectId): Document {

        val shortId = objId.get6DigiId()

        val doc = Document("_id", objId)
                .append("_shortId", shortId)
                .append("orgUuid", organization.uuid.toString())
                .append("pcuCode", organization.pcuCode)
                .append("name", organization.name)
                .append("token", organization.token.toString())
                .append("idOrg", organization.id)

                .append("lastKnownIp", organization.lastKnownIp)
                .append("firebaseToken", organization.firebaseToken)

        return doc
    }

    private fun docToObj(doc: Document): Organization {


        printDebug("\t\t\t1")
        val organization = Organization(UUID.fromString(doc["orgUuid"].toString())) {
            id = doc["idOrg"].toString()
        }

        printDebug("\t\t\t2")
        organization.pcuCode = doc["pcuCode"].toString()
        printDebug("\t\t\t3")
        organization.name = doc["name"].toString()
        printDebug("\t\t\t4")
        organization.token = UUID.fromString(doc["token"].toString())
        printDebug("\t\t\t5")
        organization.id = doc["idOrg"].toString()
        printDebug("\t\t\t6")

        organization.lastKnownIp = doc["lastKnownIp"].toString()
        printDebug("\t\t\t7")
        organization.firebaseToken = (doc["firebaseToken"] ?: null)?.toString()
        printDebug("\t\t\t8")

        return organization
    }


    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = Document("idOrg", orgId)
        if (isOrg) {
            val firebaseToken = Document("firebaseToken", firebaseToken)
            coll2.updateOne(query, BasicDBObject("\$set", firebaseToken))

        } else {
            coll2.updateOne(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }


    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
