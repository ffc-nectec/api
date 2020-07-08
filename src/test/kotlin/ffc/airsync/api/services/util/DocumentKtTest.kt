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

package ffc.airsync.api.services.util

import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should contain all`
import org.amshove.kluent.`should not be equal to`
import org.bson.types.ObjectId
import org.junit.Test

class DocumentKtTest {
    @Test
    fun userToDocument() {
        val genId = ObjectId().toHexString()
        val user =
            User(genId, "Thanachai", "cloud.topexe.xyz", User.Role.ADMIN, User.Role.PATIENT, User.Role.SYNC_AGENT)

        val userDoc = user.toDocument()

        userDoc["id"] as String `should be equal to` genId
        userDoc["name"] as String `should be equal to` "Thanachai"
        userDoc["password"] as String `should not be equal to` "cloud.topexe.xyz" // because hash
        (userDoc["roles"] as List<*>) `should contain all` listOf("ADMIN", "PATIENT", "SYNC_AGENT")
    }

    @Test
    fun userToDocumentUpdatePassword() {
        val genId = ObjectId().toHexString()
        val password = "cloud.topexe.xyz"
        val user: User =
            User(genId, "Thanachai", password, User.Role.ADMIN, User.Role.PATIENT, User.Role.SYNC_AGENT)
                .toJson()
                .parseTo()
        user.bundle["password"] = password
        user.bundle["test"] = "test2"
        val userDoc = user.toDocument()
        val bundle = userDoc["bundle"] as Map<String, String>

        bundle["test"].toString() `should be equal to` "test2"
        bundle["password"] `should be` null
    }
}
