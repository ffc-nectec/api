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

import ffc.airsync.api.MongoDbTestRule
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class MongoOtpDaoTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()
    private val timestamp = Date((System.currentTimeMillis() / 1000) * 1000) // clear sec

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    private val ORG_ID2 = "5bbd7f5ebc920637b04c7799"
    lateinit var dao: OtpDao

    @Before
    fun initDb() {
        dao = MongoOtpDao(1, TimeUnit.SECONDS)
    }

    @Test
    fun get() {
        val otp = dao.get(ORG_ID)
        otp.isNotBlank() `should be equal to` true
    }

    @Test
    fun getOtpIs6Lenght() {
        val otp = dao.get(ORG_ID)
        otp.length `should be equal to` 6
    }

    @Test
    fun getOtpIsDigiNumber() {
        val otp = dao.get(ORG_ID)
        Regex("""^\d+$""").matches(otp) `should be equal to` true
    }

    @Test
    fun getNotExpire() {
        val otp = dao.get(ORG_ID, timestamp)
        dao.isValid(ORG_ID, otp, timestamp.plusMillis(200)) `should be equal to` true
    }

    @Test
    fun getExpire() {
        val otp = dao.get(ORG_ID, timestamp)
        dao.isValid(ORG_ID, otp, timestamp.plusMillis(1500)) `should be equal to` false
    }

    @Test
    fun validateFail() {
        dao.isValid(ORG_ID, "333333") `should be equal to` false
    }

    @Test
    fun doNotAcceptOtherOrganizations() {
        val otp1 = dao.get(ORG_ID, timestamp)
        val otp2 = dao.get(ORG_ID2, timestamp)

        dao.isValid(ORG_ID, otp2, timestamp) `should be equal to` false
        dao.isValid(ORG_ID2, otp1, timestamp) `should be equal to` false
    }

    @Test
    fun acceptMyOrganizations() {
        val otp1 = dao.get(ORG_ID, timestamp)
        val otp2 = dao.get(ORG_ID2, timestamp)

        dao.isValid(ORG_ID, otp1, timestamp.plusMillis(200)) `should be equal to` true
        dao.isValid(ORG_ID2, otp2, timestamp.plusMillis(500)) `should be equal to` true
    }

    @Test
    fun get2OtpButAcceptOtp1_Otp1NotExpire() {
        val otpFirst = dao.get(ORG_ID, timestamp)
        dao.get(ORG_ID2, timestamp)
        val otpSecount = dao.get(ORG_ID, timestamp.plusMillis(500))
        println("otp1 $otpFirst otp2 $otpSecount")
        dao.isValid(ORG_ID, otpFirst, timestamp.plusMillis(500)) `should be equal to` true
        otpFirst `should equal` otpSecount
    }

    private fun Date.plusMillis(millis: Int): Date {
        val joda = DateTime(time)
        return Date(joda.plusMillis(millis).millis)
    }
}
