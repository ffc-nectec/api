package ffc.airsync.api.security

import org.mindrot.jbcrypt.BCrypt

class BcryptPassword : Password {

    override fun hash(plain: String): String {
        val salt = BCrypt.gensalt(10)
        return BCrypt.hashpw(plain, salt)
    }

    override fun check(plain: String, hash: String): Boolean {
        return BCrypt.checkpw(plain, hash)
    }
}
