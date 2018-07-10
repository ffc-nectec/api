package ffc.airsync.api.security

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be equal to`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BcryptPasswordTest(val plain: String) {

    val password: Password = BcryptPassword()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
                arrayOf("123456"),
                arrayOf("qwerty") ,
                arrayOf("password") ,
                arrayOf("h()!yshiTP@ssw0rd")
        )
    }

    @Test
    fun hashThenCheck() {
        val hash = password.hash(plain)

        password.check(plain, hash) `should be` true
    }

    @Test
    fun samePassShouldNotSameHash() {
        password.hash(plain) `should not be equal to` password.hash(plain)
    }
}


