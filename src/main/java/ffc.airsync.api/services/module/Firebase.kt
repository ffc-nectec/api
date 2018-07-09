package ffc.airsync.api.services.module

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import ffc.airsync.api.printDebug
import ffc.entity.House
import java.util.concurrent.ExecutionException

fun Message.Builder.putHouseData(address: House, registrationToken: String, orgId: String) {
    if (registrationToken.trim().isEmpty()) return
    val message = Message.builder().putData("type", "House").putData("_id", address.id).putData("url", "$orgId/place/house/${address.id}").setToken(registrationToken).build()

    var response: String? = null

    try {
        response = FirebaseMessaging.getInstance().sendAsync(message).get()
    } catch (e: InterruptedException) {
        // e.printStackTrace()
    } catch (e: ExecutionException) {
        // e.printStackTrace()
    }
    printDebug("Successfully sent message: $response")
}
