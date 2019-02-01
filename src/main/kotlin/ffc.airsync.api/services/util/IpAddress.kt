package ffc.airsync.api.services.util

import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.ipAddress(): String {
    return getHeader("X-Forwarded-For") ?: remoteAddr
}
