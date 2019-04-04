package ffc.airsync.api.services.otp

interface OtpDao {
    fun get(orgId: String): String
    fun forceNextOpt(orgId: String): String
    fun validateOk(orgId: String, otp: String): Boolean
}
