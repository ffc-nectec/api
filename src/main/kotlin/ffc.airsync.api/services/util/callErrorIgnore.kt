package ffc.airsync.api.services.util

fun callErrorIgnore(call: () -> Unit) {
    try {
        call()
    } catch (ignore: Exception) {
    }
}
