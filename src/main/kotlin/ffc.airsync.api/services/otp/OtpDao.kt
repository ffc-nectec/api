package ffc.airsync.api.services.otp

import java.util.Date

interface OtpDao {
    fun get(orgId: String, timestamp: Date = Date(System.currentTimeMillis())): String
    fun isValid(orgId: String, otp: String, timestamp: Date = Date(System.currentTimeMillis())): Boolean
}
