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
import ffc.entity.gson.toJson
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import ffc.entity.place.House

fun <T : Entity> NotifactionDao.broadcastMessage(orgId: String, vararg entitys: T) {
    val clientAddress = getFirebaseToken(orgId)
    for (entity in entitys) {
        try {
            when (entity) {
                is House -> send(entity, clientAddress, House::class.java.simpleName, PART_HOUSESERVICE)
                is HealthCareService -> send(entity, clientAddress, HealthCareService::class.java.simpleName, PART_HEALTHCARESERVICE)
                is HomeVisit -> send(entity, clientAddress, HomeVisit::class.java.simpleName, PART_HEALTHCARESERVICE)
                else -> send(entity, clientAddress, entity.type, "else")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

private fun <T : Entity> send(entity: T, clientAddress: List<String>, type: String, urlPart: String) {
    printDebug("FB token = $clientAddress $type = ${entity.toJson()}")
    clientAddress.forEach {
        if (it.isNotEmpty())
            putEntityToFirebase(entity, it, urlPart, type)
    }
}

private fun <T : Entity> putEntityToFirebase(
    entity: T,
    registrationToken: String,
    urlPart: String,
    type: String
) {
    if (registrationToken.trim().isEmpty()) {
        return
    }
    val message = Message.builder()
        .putData("type", type)
        .putData("id", entity.id)
        .putData("url", "/$urlPart/${entity.id}").setToken(registrationToken).build()
    try {
        val response = FirebaseMessaging.getInstance().sendAsync(message).get()
        printDebug("Successfully sent firebase message response = $response")
    } catch (ex: java.lang.Exception) {
        check(false) { "Fail send message firebase \n ${ex.toJson()}" }
    }
}
