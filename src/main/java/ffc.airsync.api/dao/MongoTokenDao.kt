package ffc.airsync.api.dao

import ffc.entity.StorageOrg
import ffc.entity.TokenMessage
import org.bson.Document
import org.joda.time.DateTime
import java.util.*
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class MongoTokenDao(host: String, port: Int, databaseName: String, collection: String) : TokenDao, MongoAbsConnect(host, port, databaseName, collection) {


    private fun objToDoc(tokenObj: StorageOrg<TokenMessage>): Document {

        val tokenDoc = Document("orgUuid", tokenObj.uuid.toString())
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
        val query = Document("orgUuid", uuid.toString())
                .append("user", user)
        coll2.deleteMany(query)

        val tokenMessage = TokenMessage(token = token, name = user, role = type)
        val storageOrg = StorageOrg<TokenMessage>(
                uuid = uuid,
                orgId = orgId,
                user = user,
                data = tokenMessage
        )

        val tokenDoc = objToDoc(storageOrg)


        coll2.insertOne(tokenDoc)
        return tokenMessage
    }


    override fun find(token: UUID): StorageOrg<TokenMessage> {
        val query = Document("token", token.toString())
        val tokenDoc = coll2.find(query).first()
                ?: throw NotAuthorizedException("Not auth can't find token in m token.")

        return docToObj(tokenDoc)

    }

    override fun findByOrgUuid(orgUUID: UUID): List<StorageOrg<TokenMessage>> {

        val tokenList = arrayListOf<StorageOrg<TokenMessage>>()

        val query = Document("orgUuid", orgUUID.toString())
        val tokenListDoc = coll2.find(query) ?: throw NotFoundException("ไม่พบรายการ token ใน org นี้")

        tokenListDoc.forEach {

            val tokenDoc = it
            val token = docToObj(tokenDoc)
            tokenList.add(token)
        }
        return tokenList

    }

    override fun remove(token: UUID) {
        val query = Document("token", token.toString())
        coll2.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบรายการ token นี้")

    }

    override fun updateFirebaseToken(token: UUID, firebaseToken: String) {

        val query = Document("token", token.toString())

        val update = coll2.find(query).first()
        update["firebaseToken"] = firebaseToken

        //val update = BasicDBObject("firebaseToken", firebaseToken)
        coll2.replaceOne(query, update)

    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        val query = Document("orgUuid", orgUUID.toString())
        coll2.deleteMany(query)
    }


    private fun docToObj(tokenDoc: Document): StorageOrg<TokenMessage> {
        val orgUuidStr = tokenDoc["orgUuid"].toString()
        val tokenStr = tokenDoc["token"].toString()
        val userStr = tokenDoc["user"].toString()
        val roleStr = tokenDoc["role"].toString()
        val orgIdStr = tokenDoc["orgId"].toString()
        val firebaseTokenStr = tokenDoc["firebaseToken"]?.toString()
        val timestampStr = tokenDoc["timestamp"].toString()
        val expireDateStr = tokenDoc["expireDate"].toString()

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
