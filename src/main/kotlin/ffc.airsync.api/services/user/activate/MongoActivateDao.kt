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

package ffc.airsync.api.services.user.activate

import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.plus
import ffc.entity.User
import ffc.entity.gson.parseTo
import org.bson.Document
import org.bson.types.ObjectId

class MongoActivateDao : ActivateDao, MongoDao("ffc", "organ") {
    override fun checkActivate(orgId: String, userId: String): Boolean {
        getUser(orgId, userId).let {
            return try {
                it.activate()
                false
            } catch (ex: IllegalStateException) {
                true
            }
        }
    }

    override fun setActivate(orgId: String, userId: String): User {
        dbCollection.updateOne(
            ("_id" equal ObjectId(orgId)) plus ("users.id" equal userId),
            "\$set" equal ("users.$.isActivated" equal true)
        )

        return getUser(orgId, userId)
    }

    private fun getUser(orgId: String, userId: String): User {
        val userListDoc = dbCollection.find(
            ("_id" equal ObjectId(orgId)) plus ("users.id" equal ("\$eq" equal userId))
        ).projection("users" equal 1).toMutableList().first()

        val userActivate = (userListDoc["users"] as List<Document>).first {
            it["id"] == userId
        }

        return userActivate.toJson().parseTo()
    }
}
