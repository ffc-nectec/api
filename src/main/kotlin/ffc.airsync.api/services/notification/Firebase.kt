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

package ffc.airsync.api.services.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import ffc.airsync.api.printDebug
import ffc.airsync.api.services.healthcareservice.PART_HEALTHCARESERVICE
import ffc.airsync.api.services.house.PART_HOUSESERVICE
import ffc.entity.Entity
import ffc.entity.House
import ffc.entity.gson.toJson
import ffc.entity.healthcare.HealthCareService

fun Message.Builder.broadcastHouse(address: House, registrationToken: String, orgId: String) {
    printDebug("Org id = $orgId FB token = $registrationToken House = ${address.toJson()}")
    putEntityToFirebase(address, registrationToken, orgId, PART_HOUSESERVICE, "House")
}

fun Message.Builder.broadcastVisit(
    healthCareService: HealthCareService,
    registrationToken: List<String>,
    orgId: String
) {
    printDebug("Boradcast visit by firebase = $orgId Data = ${healthCareService.toJson()}")

    try {
        registrationToken.forEach {
            putEntityToFirebase(healthCareService, it, orgId, PART_HEALTHCARESERVICE, "HealthCare")
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

private fun putEntityToFirebase(
    entity: Entity,
    registrationToken: String,
    orgId: String,
    urlPart: String,
    type: String
) {
    if (registrationToken.trim().isEmpty()) {
        return
    }
    val message = Message.builder()
        .putData("type", type)
        .putData("id", entity.id)
        .putData("url", "$orgId/$urlPart/${entity.id}").setToken(registrationToken).build()
    try {
        val response = FirebaseMessaging.getInstance().sendAsync(message).get()
        printDebug("Successfully sent firebase message response = $response")
    } catch (ex: java.lang.Exception) {
        check(false) { "Fail send message firebase \n ${ex.toJson()}" }
    }
}
