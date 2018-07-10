package ffc.airsync.api.security

interface Password{

    fun hash(plain: String) : String

    fun check(plain: String, hash: String) : Boolean
}
