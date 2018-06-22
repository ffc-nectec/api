package ffc.airsync.api.services.module

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import ffc.airsync.api.printDebug
import ffc.entity.Address
import java.util.concurrent.ExecutionException


fun Message.Builder.putHouseData(address: Address, registrationToken: String, orgId: String) {
    val message = Message.builder()
            .putData("type", "House")
            .putData("_id", address._id)
            .putData("url", "$orgId/place/house/${address._id}")
            .setToken(registrationToken)
            .build()


    var response: String? = null


    try {
        response = FirebaseMessaging.getInstance().sendAsync(message).get()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } catch (e: ExecutionException) {
        e.printStackTrace()
    }
    printDebug("Successfully sent message: " + response!!)
}
