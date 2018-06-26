package ffc.airsync.api.dao

import ffc.entity.Token
import ffc.entity.parseTo
import ffc.entity.toJson
import org.bson.Document
import org.bson.types.ObjectId
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class MongoTokenDao(host: String, port: Int, databaseName: String, collection: String) : TokenDao, MongoAbsConnect(host, port, databaseName, collection) {


    override fun create(user: String, orgId: String, type: Token.TYPEROLE): Token {

        val generateToken = ObjectId()


        val tokenMessage = Token(token = generateToken.toHexString(), name = user, role = type)

        val tokenDoc = Document.parse(tokenMessage.toJson())
        tokenDoc.append("orgId", orgId)
        tokenDoc.append("_id", generateToken)


        coll2.insertOne(tokenDoc)
        return tokenMessage
    }

    override fun find(token: String): Token {
        val query = Document("token", token)
        val tokenDoc = coll2.find(query).first()
                ?: throw NotAuthorizedException("Not auth can't find token in m token.")

        return tokenDoc.toJson().parseTo()

    }

    override fun findByOrgId(orgId: String): List<Token> {

        val tokenList = arrayListOf<Token>()

        val query = Document("orgId", orgId)
        val tokenListDoc = coll2.find(query) ?: throw NotFoundException("ไม่พบรายการ token ใน org นี้")

        tokenListDoc.forEach {

            val tokenDoc = it
            val token: Token = tokenDoc.toJson().parseTo()
            tokenList.add(token)
        }
        return tokenList

    }

    override fun remove(token: String) {
        val query = Document("token", token.toString())
        coll2.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบรายการ token นี้")

    }

    override fun removeByOrgId(orgId: String) {
        val query = Document("orgId", orgId)
        coll2.deleteMany(query)
    }

}
