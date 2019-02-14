package ffc.airsync.api.services.util

import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.ipAddress(): String {
    return getHeader("X-Real-IP") ?: (getHeader("X-Forwarded-For") ?: remoteAddr)
}
