package ffc.airsync.api.services.otp

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be in`
import org.amshove.kluent.`should not be less than`
import org.junit.Test

class RandomSecretStringTest {

    @Test
    fun checkRandomNumber() {
        runBlocking {
            repeat(200) {
                Regex("""^[\d\w]+$""").matches(secretString.getSecret()) `should equal` true
            }
        }
    }

    @Test
    fun duplicateKey() {
        val check = arrayListOf<String>()
        runBlocking {
            repeat(100) {
                val otp = secretString.getSecret()
                otp `should not be in` check
                check.add(otp)
            }
        }
    }

    @Test
    fun moreThan60() {
        secretString.getSecret().length `should not be less than` 60
    }
}
