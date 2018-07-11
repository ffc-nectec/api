package ffc.airsync.api.dao

// TODO("สำหรับใช้งาน Firebase รอการพัฒนา")
interface FirebaseTokenDao : Dao {

    fun addMobileToken(orgId: String, token: String)
    fun addOrgToken(orgId: String, token: String)
    fun findToken(orgId: String): List<String>
    fun findMobileToken(orgId: String): List<String>
    fun findOrgToken(orgId: String): String?
}
