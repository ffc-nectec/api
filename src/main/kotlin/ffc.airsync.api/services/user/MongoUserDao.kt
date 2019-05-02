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

package ffc.airsync.api.services.user

import com.google.gson.Gson
import ffc.airsync.api.security.password
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.plus
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.ffcGson
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId

internal class MongoUserDao : UserDao, MongoDao("ffc", "organ") {

    override fun insertUser(user: User, orgId: String): User {
        require(!user.isActivated) { "User ที่จะเพิ่มเข้ามาใหม่ ต้อง isActivated=false" }
        if (!haveUserInDb(orgId, user)) {
            user.orgId = orgId
            require(user.isTempId) { "รุปแบบ id ต้องใช้ TempId ในการสร้าง User" }
            val userStruct = "users" equal user.toDocument()
            val userPush = "\$push" equal userStruct

            dbCollection.updateOne("_id" equal ObjectId(orgId), userPush)
        }
        return findUser(orgId).find { it.name == user.name }
            ?: throw IllegalStateException("Server Error in call dev 999148")
    }

    private fun haveUserInDb(orgId: String, user: User): Boolean {
        val userDuplicate = getUserByName(orgId, user.name)
        return (userDuplicate != null)
    }

    override fun getUserByName(orgId: String, name: String): User? {
        val userInDb = dbCollection.find("_id" equal ObjectId(orgId)).projection("users" equal 1).first()
        val userList: Array<User>? = userInDb?.get("users")?.toJson()?.parseTo()
        return userList?.find {
            it.name == name
        }
    }

    override fun getUserById(orgId: String, userId: String): User {
        return getUserDocument(orgId, userId).toJson().parseTo()
    }

    override fun updateUser(user: User, orgId: String): User {
        val userInDb = getUserDocument(orgId, user.id)
        getUserById(orgId, user.id)
        user.orgId = orgId

        val userDocument = Document.parse(user.toJson())
        userDocument.append("password", userInDb["password"])

        val query = ("_id" equal ObjectId(orgId)) plus ("users.id" equal user.id)
        val set = "\$set" equal ("users.$" equal userDocument)

        dbCollection.updateOne(query, set)

        return getUserById(orgId, user.id)
    }

    private fun getUserDocument(orgId: String, userId: String): Document {
        val query = "_id" equal ObjectId(orgId)
        val userList =
            dbCollection.find(query).projection("users" equal 1).firstOrNull()?.get("users") as List<Document>
        return userList.find { it["id"] == userId }
            ?: throw NoSuchElementException("ไม่พบ user Id $userId")
    }

    override fun findUser(orgId: String): List<User> {
        val userDocList = dbCollection
            .find("_id" equal ObjectId(orgId)).projection("users" equal 1).first() ?: return arrayListOf()
        return userDocList.getAs("users") ?: listOf()
    }

    inline fun <reified T> Document.getAs(key: String, gson: Gson = ffcGson): T? {
        return this[key]?.toJson(gson)?.parseTo()
    }

    override fun findThat(orgId: String, name: String, password: String): User? {
        val orgDoc = dbCollection.find("_id" equal ObjectId(orgId)).first() ?: return null
        val org = orgDoc.toJson().parseTo<Organization>()
        val user = org.users.find { it.name == name }

        return if (user != null && password().check(password, user.password)) user else null
    }
}
