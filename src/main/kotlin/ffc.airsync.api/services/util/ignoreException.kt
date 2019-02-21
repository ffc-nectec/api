package ffc.airsync.api.services.util

fun ignoreException(call: () -> Unit) {
    try {
        call()
    } catch (ignore: Exception) {
    }
}
