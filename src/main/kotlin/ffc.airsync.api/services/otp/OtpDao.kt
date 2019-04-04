package ffc.airsync.api.services.otp

interface OtpDao {
    fun get(orgId: String): String
    fun isValid(orgId: String, otp: String): Boolean
}
