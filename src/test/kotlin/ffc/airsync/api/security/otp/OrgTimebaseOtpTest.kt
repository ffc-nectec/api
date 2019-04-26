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

package ffc.airsync.api.security.otp

import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.mock
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import java.util.Date

class OrgTimebaseOtpTest {
    val mockSecretStore: SecretStore = mock()
    val ORG_ID = "5bbd7f5ebc920637b04c7796"
    val orgTimebaseOtp: OrgTimebaseOtp = OrgTimebaseOtp(ORG_ID, mockSecretStore)
    @Before
    fun setUp() {
        When calling mockSecretStore.secretOf("5bbd7f5ebc920637b04c7796") itReturns "secretXXXkey"
    }

    @Test
    fun generate() {
        orgTimebaseOtp.generate().isNotBlank() `should be equal to` true
    }

    @Test
    fun verifyFail() {
        orgTimebaseOtp.verify("999999") `should be equal to` false
    }

    @Test
    fun verifyNormal() {
        orgTimebaseOtp.verify(orgTimebaseOtp.generate()) `should be equal to` true
    }

    private fun Date.minusSeconds(sec: Int): Date {
        val joda = DateTime(time)
        return Date(joda.minusSeconds(sec).millis)
    }
}
