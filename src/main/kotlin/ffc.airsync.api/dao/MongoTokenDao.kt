package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.Token
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId

internal class MongoTokenDao(host: String, port: Int) : TokenDao, MongoAbsConnect(host, port, "ffc", "token") {
    override fun create(user: User, orgId: String): Token {
        val generateToken = ObjectId()
        val tokenMessage = Token(token = generateToken.toHexString(), user = user)
        val tokenDoc = Document.parse(tokenMessage.toJson())
        tokenDoc.append("orgId", orgId)
        tokenDoc.append("_id", generateToken)
        dbCollection.insertOne(tokenDoc)
        return tokenMessage
    }

    override fun find(token: String): Token? {
        printDebug("Token Dao find $token")
        val query = Document("token", token)
        val tokenDoc = dbCollection.find(query).first()
        printDebug("\tResult token find $tokenDoc")
        if (tokenDoc == null) return null
        return tokenDoc.toJson().parseTo()
    }

    override fun findByOrgId(orgId: String): List<Token> {
        val tokenList = arrayListOf<Token>()

        val query = Document("orgId", orgId)
        val tokenListDoc = dbCollection.find(query)

        tokenListDoc.forEach {
            val tokenDoc = it
            val token: Token = tokenDoc.toJson().parseTo()
            tokenList.add(token)
        }
        return tokenList
    }

    override fun remove(token: String): Boolean {
        val query = Document("token", token)
        val tokenDoc = dbCollection.findOneAndDelete(query)
        return tokenDoc != null
    }

    override fun removeByOrgId(orgId: String) {
        val query = Document("orgId", orgId)
        dbCollection.deleteMany(query)
    }
}
