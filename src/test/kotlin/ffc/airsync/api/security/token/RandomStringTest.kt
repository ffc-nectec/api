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
 *
 */

package ffc.airsync.api.security.token

import ffc.airsync.api.security.SecretRandom
import org.amshove.kluent.`should not be equal to`
import org.amshove.kluent.`should not be less than`
import org.junit.Test

class RandomStringTest {
    @Test
    fun randomString() {
        val ranToken = SecretRandom()
        val rand1 = ranToken.nextSecret()
        val rand2 = ranToken.nextSecret()

        rand1 `should not be equal to` rand2
        rand1.length `should not be less than` 20
        rand2.length `should not be less than` 20
    }
}
