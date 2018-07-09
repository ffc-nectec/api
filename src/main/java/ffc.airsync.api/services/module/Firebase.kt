package ffc.airsync.api.services.module

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import ffc.airsync.api.printDebug
import ffc.entity.House
import ffc.entity.gson.toJson

fun Message.Builder.putHouseData(address: House, registrationToken: String, orgId: String) {
    printDebug("Org id = $orgId FB token = $registrationToken House = ${address.toJson()}")
    if (registrationToken.trim().isEmpty()) {
        return
    }
    val message = Message.builder().putData("type", "House").putData("_id", address.id).putData("url", "$orgId/place/house/${address.id}").setToken(registrationToken).build()

    val response = FirebaseMessaging.getInstance().sendAsync(message).get()

    printDebug("Successfully sent message: $response")
}
