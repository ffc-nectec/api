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

import com.marcelkliemannel.kotlinonetimepassword.HmacAlgorithm
import com.marcelkliemannel.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import com.marcelkliemannel.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import ffc.airsync.api.services.MongoDao
import org.joda.time.DateTime
import java.util.Date
import java.util.concurrent.TimeUnit

interface Otp {

    fun generate(): String

    fun verify(otp: String): Boolean
}

class OrgTimebaseOtp(
    val orgId: String,
    private val secretStore: SecretStore = MongoSecretStore(),
    private val clock: Clock = JodaClock()
) : Otp {

    private val config = TimeBasedOneTimePasswordConfig(60, TimeUnit.SECONDS, 6, HmacAlgorithm.SHA512)

    private val secret: String by lazy { secretStore.secretOf(orgId) }
    private val generator by lazy { TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config) }

    override fun generate(): String {
        return generator.generate(clock.now())
    }

    internal fun generateLeeway(): String {
        return generator.generate(clock.now().minusMillis(60000))
    }

    private fun Date.minusMillis(millis: Int): Date {
        val joda = DateTime(time)
        return Date(joda.minusMillis(millis).millis)
    }

    override fun verify(otp: String): Boolean {
        if (otp == generate())
            return true
        return otp == generateLeeway()
    }
}

interface SecretStore {

    fun secretOf(orgId: String): String
}

class MongoSecretStore : SecretStore, MongoDao("ffc", "secret") {

    override fun secretOf(orgId: String): String {
        TODO(" implemented")
        return "secret"
    }
}

interface Clock {

    fun now(): Date
}

class JodaClock : Clock {

    override fun now(): Date {
        return DateTime.now().toDate()
    }
}

