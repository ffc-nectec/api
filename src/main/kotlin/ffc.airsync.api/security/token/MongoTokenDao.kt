/*
 * Copyright (c) 2019 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ffc.airsync.api.security.token

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import ffc.airsync.api.getLogger
import ffc.airsync.api.security.SecretRandom
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ignoreException
import ffc.entity.Token
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.BsonDateTime
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

internal class MongoTokenDao : TokenDao, MongoDao("ffc", "token") {
    init {
        ignoreException {
            dbCollection.createIndex(Indexes.hashed("token"))
        }
        ignoreException {
            dbCollection.createIndex(Indexes.ascending("MongoCreated"), IndexOptions().expireAfter(7L, TimeUnit.DAYS))
        }
    }

    override fun create(user: User, orgId: String): Token {
        val generateId = ObjectId()
        val tokenMessage = Token(token = SecretRandom().nextSecret(), user = user)
        val tokenDoc = Document.parse(tokenMessage.toJson())
        tokenDoc.append("orgIndex", ObjectId(orgId))
        tokenDoc.append("_id", generateId)
        if (user.roles.contains(User.Role.SYNC_AGENT))
            tokenDoc.append("MongoCreated", BsonDateTime(DateTime.now().plusYears(1000).millis))
        else
            tokenDoc.append("MongoCreated", BsonDateTime(DateTime.now().millis))
        dbCollection.insertOne(tokenDoc)
        return tokenMessage
    }

    override fun token(token: String, orgId: String): Token? {
        getLogger().debug("Object token $token in org[$orgId]")
        val tokenDoc =
            dbCollection.find("token" equal token.trim()).first() ?: return null
        if (tokenDoc["orgIndex"].toString() != orgId.trim()) return null
        return tokenDoc.toJson().parseTo()
    }

    override fun findByOrgId(orgId: String): List<Token> {
        return dbCollection.find("orgIndex" equal ObjectId(orgId))
            .map { it.toJson().parseTo<Token>() }.toList()
    }

    override fun remove(token: String): Boolean {
        val query = "token" equal token
        val tokenDoc =
            dbCollection.find(query).firstOrNull()
        dbCollection.deleteMany(query)
        return tokenDoc != null
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}
