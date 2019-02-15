package ffc.airsync.api.services.util

import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.forwardForIpAddress(): String {
    return (getHeader("X-Forwarded-For") ?: remoteAddr)
}

fun HttpServletRequest.realIpAddress(): String? {
    return getHeader("X-Real-IP")
}

fun HttpServletRequest.firstForwardForAddress(): String {
    return getFirstIp((getHeader("X-Forwarded-For") ?: remoteAddr))
}

internal fun getFirstIp(ipGroup: String): String {
    return ipGroup.split(',').first().trim()
}

private fun String.getIpList(): List<String> {
    return this.split(',').map {
        it.trim()
    }
}
