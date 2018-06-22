package ffc.airsync.api.dao

//import java.util.*
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import ffc.airsync.api.get6DigiId
import ffc.airsync.api.printDebug
import ffc.entity.Organization
import org.bson.types.ObjectId
import java.util.*
import javax.ws.rs.NotFoundException

class MongoOrgDao(host: String, port: Int, databaseName: String, collection: String) : OrgDao, MongoAbsConnect(host, port, databaseName, collection) {

    companion object {
        private val COUNTERNAME = "idorg"

    }

    private val couterColl: DBCollection


    init {


        try {
            if (mongoUrl.isEmpty() || mongoUrl.startsWith("null")) {
                printDebug("\tCall create counter by object.")
                couterColl = getClient()!!.getDB(dbName).getCollection("counter")
                printDebug("\t\tFinish call create counter.")
            } else {
                printDebug("\tCall create counter by url string parameter.")
                couterColl = getClient()!!.getDB(System.getenv("MONGODB_DBNAME")).getCollection("counter")
                printDebug("\t\tFinish call create counter.")
            }


            val counterDoc = BasicDBObject("_id", COUNTERNAME)
                    .append("sec", 1)

            try {
                couterColl.insert(counterDoc)
            } catch (exk: com.mongodb.DuplicateKeyException) {
                printDebug("Org Counter Duplicate.")
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
        val queryRemove = BasicDBObject("orgUuid", organization.uuid.toString())
        coll.remove(queryRemove)


        val queryIndex = BasicDBObject("_id", COUNTERNAME)
        val updateIndex = BasicDBObject("\$inc",
                BasicDBObject("sec", 1))
        val newIndex = couterColl.findAndModify(queryIndex, updateIndex)
        val valuetext = newIndex.get("sec").toString()

        printDebug("Counter counter $valuetext")



        organization.id = valuetext

        //uuid id token ipaddress
        val doc = createDoc(organization, ObjectId())
        coll.insert(doc)


    }

    override fun find(): List<Organization> {
        printDebug("Mongo find() org")
        val orgCursorList = coll.find()
        val orgList = docListToObj(orgCursorList)
        if (orgList.size < 1) throw NotFoundException("ไม่พบรายการ org ลงทะเบียน ในระบบ")
        return orgList

    }

    override fun findByUuid(uuid: UUID): Organization {
        printDebug("Mongo find org uuid $uuid")
        val query = BasicDBObject("orgUuid", uuid.toString())
        val doc = coll.findOne(query) ?: throw NotFoundException("ไม่พบ uuid ${uuid.toString()} ที่ค้นหา")
        val organization = docToObj(doc)

        return organization
    }

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        printDebug("Mongo find org ip $ipAddress")
        val query = BasicDBObject("lastKnownIp", ipAddress)
        printDebug("\tCreate query object $query")
        val orgDoc = coll.find(query)
        printDebug("\tQuery org from mongo $orgDoc")
        val orgList = docListToObj(orgDoc)

        if (orgList.size < 1) throw NotFoundException("ไม่พบรายการลงทะเบียนในกลุ่มของ Org ip $ipAddress")

        return orgList
    }

    override fun findByToken(token: UUID): Organization {
        printDebug("Mongo find org token $token")
        val query = BasicDBObject("token", token.toString())
        val doc = coll.findOne(query) ?: throw NotFoundException("ไม่พบ token ${token.toString()} ที่ค้นหา")
        val organization = docToObj(doc)

        return organization

    }

    override fun findById(id: String): Organization {
        printDebug("Mongo find org id $id")
        val query = BasicDBObject("idOrg", id)
        val doc = coll.findOne(query) ?: throw NotFoundException("ไม่พบ id org $id ที่ค้นหา")
        val organization = docToObj(doc)

        return organization
    }

    override fun remove(organization: Organization) {
        printDebug("Mongo remove org $organization")
        val query = BasicDBObject("orgUuid", organization.uuid.toString())
        coll.remove(query)
    }

    override fun updateToken(organization: Organization): Organization {
        printDebug("Mongo update token orgobj=$organization")
        val query = BasicDBObject("orgUuid", organization.uuid.toString())
        val oldDoc = coll.findOne(query)
                ?: throw NotFoundException("ไม่พบ Object organization ${organization.uuid} ให้ Update")


        organization.token = UUID.randomUUID()
        val updateDoc = createDoc(organization, ObjectId(oldDoc.get("_id").toString()))

        coll.update(oldDoc, updateDoc)

        return organization
    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        printDebug("Mongo remove org uuid $orgUUID")
        val query = BasicDBObject("orgUuid", orgUUID.toString())

        val doc = coll.findAndRemove(query) ?: throw NotFoundException()

    }

    private fun createDoc(organization: Organization, objId: ObjectId): BasicDBObject {

        val shortId = objId.get6DigiId()

        val doc = BasicDBObject("_id", objId)
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

    private fun docToObj(doc: DBObject): Organization {


        printDebug("\t\t\t1")
        val organization = Organization(UUID.fromString(doc.get("orgUuid").toString())) {
            id = doc.get("idOrg").toString()
        }

        printDebug("\t\t\t2")
        organization.pcuCode = doc.get("pcuCode").toString()
        printDebug("\t\t\t3")
        organization.name = doc.get("name").toString()
        printDebug("\t\t\t4")
        organization.token = UUID.fromString(doc.get("token").toString())
        printDebug("\t\t\t5")
        organization.id = doc.get("idOrg").toString()
        printDebug("\t\t\t6")

        organization.lastKnownIp = doc.get("lastKnownIp").toString()
        printDebug("\t\t\t7")
        organization.firebaseToken = (doc.get("firebaseToken") ?: null)?.toString()
        printDebug("\t\t\t8")

        return organization
    }

    private fun docListToObj(cursor: DBCursor): List<Organization> {

        val orgList = arrayListOf<Organization>()
        printDebug("\t\tLoad doc list.")
        while (cursor.hasNext()) {
            val it = cursor.next()
            printDebug("\t\t$it")
            val organization = docToObj(it)
            orgList.add(organization)
        }
        printDebug("\tReturn docListToObj")
        return orgList

    }


    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = BasicDBObject("idOrg", orgId)
        if (isOrg) {
            val firebaseToken = BasicDBObject("firebaseToken", firebaseToken)
            coll.update(query, BasicDBObject("\$set", firebaseToken))

        } else {
            coll.update(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }


    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
