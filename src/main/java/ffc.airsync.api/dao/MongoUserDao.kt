/*
 * Copyright (c) 2018 NECTEC
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
 */

package ffc.airsync.api.dao

import com.google.gson.Gson
import ffc.airsync.api.printDebug
import ffc.airsync.api.security.password
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.ffcGson
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId
import javax.ws.rs.NotFoundException

class MongoUserDao(host: String, port: Int)
    : UserDao, MongoAbsConnect(host, port, "ffc", "organ") {

    override fun insertUser(user: User, orgId: String): User {
        if (!user.isTempId) throw IllegalArgumentException("รุปแบบ id ต้องใช้ TempId ในการสร้าง User")
        if (haveUserInDb(orgId, user)) throw IllegalArgumentException("มีการเพิ่มผู้ใช้ ${user.name} ซ้ำ")

        val userStruct = Document("users", user.toDocument())
        val userPush = Document("\$push", userStruct)

        dbCollection.updateOne(Document("id", orgId), userPush)

        return findUser(orgId).find { it.name == user.name }
                ?: throw IllegalStateException("Server Error in call dev")
    }

    private fun haveUserInDb(orgId: String, user: User): Boolean {
        printDebug("\t\t\tCall haveUserInDb")
        val query = Document("id", orgId)
        val userInDb = dbCollection.find(query).projection(Document("users", 1)).first()
        printDebug("\t\t\tOrg in haveUserInDb = $userInDb")
        val userList: Array<User>? = userInDb?.get("users")?.toJson()?.parseTo()
        printDebug("\t\t\tUser obj = ${userList?.toJson()}")
        val userDuplicate = userList?.find {
            it.name == user.name
        }
        return (userDuplicate != null)
    }

    override fun updateUser(user: User, orgId: String): User {
        if (!haveUserInDb(orgId, user)) throw NotFoundException("ไม่พบผู้ใช้ ${user.name} ในระบบ")

        // TODO("รอพัฒนาระบบ Update User")
        // val query = Document("_id", ObjectId(orgId)).append("users", Document("name", user.name))
        return User().apply {
            name = "Dymmy"
            password = "Dymmy"
        }
    }

    override fun findUser(orgId: String): List<User> {
        printDebug("Find User in orgId $orgId")
        val query = Document("_id", ObjectId(orgId))
        val userDocList = dbCollection.find(query).projection(Document("users", 1)).first() ?: return arrayListOf()
        printDebug("\tuser list ${userDocList.toJson()}")
        return userDocList.getAs("users") ?: listOf()
    }

    inline fun <reified T> Document.getAs(key: String, gson: Gson = ffcGson): T? {
        return this.get(key)?.toJson(gson)?.parseTo()
    }

    override fun findThat(orgId: String, name: String, password: String): User? {
        val orgDoc = dbCollection.find("id" equal orgId).first() ?: return null
        val org = orgDoc.toJson().parseTo<Organization>()

        val user = org.users.find { it.name == name }

        return if (user != null && password().check(password, user.password)) user else null
    }
}
