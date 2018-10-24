package ffc.airsync.api.services.token

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.equal
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
        tokenDoc.append("orgIndex", ObjectId(orgId))
        tokenDoc.append("_id", generateToken)
        dbCollection.insertOne(tokenDoc)
        return tokenMessage
    }

    override fun find(token: String): Token? {
        printDebug("Token Dao find $token")
        val tokenDoc = dbCollection.find("token" equal token.trim()).first()
        printDebug("\tResult token find $tokenDoc")
        if (tokenDoc == null) return null
        return tokenDoc.toJson().parseTo()
    }

    override fun findByOrgId(orgId: String): List<Token> {
        return dbCollection.find("orgIndex" equal ObjectId(orgId))
            .map { it.toJson().parseTo<Token>() }.toList()
    }

    override fun remove(token: String): Boolean {
        val tokenDoc = dbCollection.findOneAndDelete("token" equal token)
        return tokenDoc != null
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}
