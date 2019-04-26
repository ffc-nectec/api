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

package ffc.airsync.api.security.otp

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be equal to`
import org.amshove.kluent.mock
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class OrgTimebaseOtpTest {

    val mockSecretStore: SecretStore = mock()
    val mockClock: Clock = mock()

    private val orgId = "102030"

    private val otp = OrgTimebaseOtp(orgId, mockSecretStore, mockClock)

    @Before
    fun setUp() {
        whenever(mockClock.now()).thenReturn(DateTime.parse("2017-01-01T00:00:00").toDate())
        whenever(mockSecretStore.secretOf(orgId)).thenReturn("xxxx")
    }

    @Test
    fun generate() {
        otp.generate() `should be equal to` "021323"
        otp.generateLeeway() `should be equal to` "459143"
    }

    @Test
    fun verify() {
        otp.verify("021323") `should be` true
    }

    @Test
    fun verifyLeeway() {
        otp.verify("459143") `should be` true
    }

    @Test
    fun diffOrgDiffOtp() {
        val orgB = "bbbbb"
        val otpB = OrgTimebaseOtp(orgB, mockSecretStore, mockClock)
        whenever(mockSecretStore.secretOf(orgB)).thenReturn("anotherSecret")

        otp.generate() `should not be equal to` otpB.generate()
    }
}
