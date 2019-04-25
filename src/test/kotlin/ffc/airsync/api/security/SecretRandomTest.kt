/*
 * Copyright (c) 2019 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.airsync.api.security

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be equal to`
import org.amshove.kluent.`should not be in`
import org.amshove.kluent.`should not be less than`
import org.junit.Test

class SecretRandomTest {

    @Test
    fun checkRandomNumber() {
        runBlocking {
            repeat(200) {
                Regex("""^[\d\w]+$""").matches(SecretRandom().nextSecret()) `should equal` true
            }
        }
    }

    @Test
    fun duplicateKey() {
        val check = arrayListOf<String>()
        runBlocking {
            repeat(100) {
                val otp = SecretRandom().nextSecret()
                otp `should not be in` check
                check.add(otp)
            }
        }
    }

    @Test
    fun moreThan60() {
        SecretRandom().nextSecret().length `should not be less than` 60
    }

    @Test
    fun randomString() {

        val ranToken = SecretRandom(21)
        val rand1 = ranToken.nextSecret()
        val rand2 = ranToken.nextSecret()

        rand1 `should not be equal to` rand2
        rand1.length `should be` 21
        rand2.length `should be` 21
    }
}
