package ffc.airsync.api.services.otp

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be in`
import org.junit.Test

class RandomOtpTest {

    private val randomOtp = RandomOtp()

    @Test
    fun checkRandomNumber() {
        runBlocking {
            repeat(200) {
                Regex("""^\d+$""").matches(randomOtp.nextOtp()) `should equal` true
            }
        }
    }

    @Test
    fun duplicateKey() {
        val check = arrayListOf<String>()
        runBlocking {
            repeat(100) {
                val otp = randomOtp.nextOtp()
                otp `should not be in` check
                check.add(otp)
            }
        }
    }
}
