package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.Token
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class MongoTokenDao(host: String, port: Int, databaseName: String, collection: String) : TokenDao, MongoAbsConnect(host, port, databaseName, collection) {
    override fun create(user: User, orgId: String): Token {
        val generateToken = ObjectId()
        val tokenMessage = Token(token = generateToken.toHexString(), user = user)
        val tokenDoc = Document.parse(tokenMessage.toJson())
        tokenDoc.append("orgId", orgId)
        tokenDoc.append("_id", generateToken)
        dbCollection.insertOne(tokenDoc)
        return tokenMessage
    }

    override fun find(token: String): Token {
        printDebug("Token Dao find $token")
        val query = Document("token", token)
        val tokenDoc = dbCollection.find(query).first()
                ?: throw NotAuthorizedException("Not auth can't find token in m token.")

        printDebug("\tResult token find $tokenDoc")

        return tokenDoc.toJson().parseTo()
    }

    override fun findByOrgId(orgId: String): List<Token> {

        val tokenList = arrayListOf<Token>()

        val query = Document("orgId", orgId)
        val tokenListDoc = dbCollection.find(query) ?: throw NotFoundException("ไม่พบรายการ token ใน org นี้")

        tokenListDoc.forEach {

            val tokenDoc = it
            val token: Token = tokenDoc.toJson().parseTo()
            tokenList.add(token)
        }
        return tokenList
    }

    override fun remove(token: String) {
        val query = Document("token", token)
        dbCollection.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบรายการ token นี้")
    }

    override fun removeByOrgId(orgId: String) {
        val query = Document("orgId", orgId)
        dbCollection.deleteMany(query)
    }
}
