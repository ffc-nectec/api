package ffc.airsync.api.services.token

import org.amshove.kluent.`should not be equal to`
import org.amshove.kluent.`should not be less than`
import org.junit.Test

class RandomStringTest {
    @Test
    fun randomString() {
        val ranToken = RandomString()
        val rand1 = ranToken.nextString()
        val rand2 = ranToken.nextString()

        rand1 `should not be equal to` rand2
        rand1.length `should not be less than` 20
        rand2.length `should not be less than` 20
    }
}
