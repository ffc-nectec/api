package ffc.airsync.api.dao

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import ffc.entity.StorageOrg
import ffc.entity.TokenMessage
import org.joda.time.DateTime
import java.util.*
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class MongoTokenDao(host: String, port: Int, databaseName: String, collection: String) : TokenDao, MongoAbsConnect(host, port, databaseName, collection) {

    private fun objToDoc(tokenObj: StorageOrg<TokenMessage>): BasicDBObject {

        val tokenDoc = BasicDBObject("orgUuid", tokenObj.uuid.toString())
                .append("token", tokenObj.data.token.toString())
                .append("user", tokenObj.user)
                .append("role", tokenObj.data.role.toString())
                .append("firebaseToken", tokenObj.data.firebaseToken)
                .append("orgId", tokenObj.orgId)
                .append("timestamp", tokenObj.data.timestamp.toString())
                .append("expireDate", tokenObj.data.expireDate.toString())

        return tokenDoc

    }


    override fun insert(token: UUID, uuid: UUID, user: String, orgId: String, type: TokenMessage.TYPEROLE): TokenMessage {
        val query = BasicDBObject("orgUuid", uuid.toString())
                .append("user", user)
        coll.remove(query)

        val tokenMessage = TokenMessage(token = token, name = user, role = type)
        val storageOrg = StorageOrg<TokenMessage>(
                uuid = uuid,
                orgId = orgId,
                user = user,
                data = tokenMessage
        )

        val tokenDoc = objToDoc(storageOrg)


        coll.insert(tokenDoc)
        return tokenMessage
    }


    override fun find(token: UUID): StorageOrg<TokenMessage> {
        val query = BasicDBObject("token", token.toString())
        val tokenDoc = coll.findOne(query)

        if (tokenDoc != null) {
            return docToObj(tokenDoc)
        } else {
            throw NotAuthorizedException("Not auth can't find token in m token.")
        }
    }

    override fun findByOrgUuid(orgUUID: UUID): List<StorageOrg<TokenMessage>> {

        val tokenList = arrayListOf<StorageOrg<TokenMessage>>()

        val query = BasicDBObject("orgUuid", orgUUID.toString())
        val tokenListDoc = coll.find(query) ?: throw NotFoundException("ไม่พบรายการ token ใน org นี้")

        if (tokenListDoc.hasNext()) {

            val tokenDoc = tokenListDoc.next()
            val token = docToObj(tokenDoc)
            tokenList.add(token)
        }
        return tokenList

    }

    override fun remove(token: UUID) {
        val query = BasicDBObject("token", token.toString())
        coll.findAndRemove(query) ?: throw NotFoundException("ไม่พบรายการ token นี้")

    }

    override fun updateFirebaseToken(token: UUID, firebaseToken: String) {

        val query = BasicDBObject("token", token.toString())

        val update = coll.findOne(query)
        update.put("firebaseToken", firebaseToken)

        //val update = BasicDBObject("firebaseToken", firebaseToken)
        coll.update(query, update)

    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        val query = BasicDBObject("orgUuid", orgUUID.toString())
        coll.remove(query)
    }


    private fun docToObj(tokenDoc: DBObject): StorageOrg<TokenMessage> {
        val orgUuidStr = tokenDoc.get("orgUuid").toString()
        val tokenStr = tokenDoc.get("token").toString()
        val userStr = tokenDoc.get("user").toString()
        val roleStr = tokenDoc.get("role").toString()
        val orgIdStr = tokenDoc.get("orgId").toString()
        val firebaseTokenAny = tokenDoc.get("firebaseToken")
        val firebaseTokenStr = if (firebaseTokenAny != null) firebaseTokenAny.toString() else null
        val timestampStr = tokenDoc.get("timestamp").toString()
        val expireDateStr = tokenDoc.get("expireDate").toString()

        val tokenMessage = TokenMessage(
                token = UUID.fromString(tokenStr),
                name = userStr,
                firebaseToken = firebaseTokenStr,
                role = enumValueOf(roleStr),
                timestamp = DateTime.parse(timestampStr)
        )
        tokenMessage.expireDate = DateTime.parse(expireDateStr)

        val storageOrg = StorageOrg<TokenMessage>(
                uuid = UUID.fromString(orgUuidStr),
                orgId = orgIdStr,
                user = userStr,
                data = tokenMessage
        )
        return storageOrg

    }
}
