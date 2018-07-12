/*
 * Copyright (c) 2018 NECTEC
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
                arrayOf("qwerty"),
                arrayOf("password"),
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
