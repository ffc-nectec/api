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

import com.marcelkliemannel.kotlinonetimepassword.HmacAlgorithm
import com.marcelkliemannel.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import com.marcelkliemannel.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import ffc.airsync.api.security.SecretRandom
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import java.util.Date
import java.util.concurrent.TimeUnit

interface Otp {

    fun generate(): String

    fun verify(otp: String): Boolean
}

class OrgTimebaseOtp(
    val orgId: String,
    val secretStore: SecretStore = MongoSecretStore()
) : Otp {

    private val config = TimeBasedOneTimePasswordConfig(
        timeStep = 60,
        timeStepUnit = TimeUnit.SECONDS,
        codeDigits = 6,
        hmacAlgorithm = HmacAlgorithm.SHA512
    )

    override fun generate(): String {
        val secret = secretStore.secretOf(orgId)
        return calculateOtp(secret)
    }

    internal fun calculateOtp(secret: String, time: Date = Date(System.currentTimeMillis())): String {
        val otpGenerator = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
        return otpGenerator.generate(time)
    }

    override fun verify(otp: String): Boolean {
        val secret = secretStore.secretOf(orgId)
        return if (otp == calculateOtp(secret))
            true
        else {
            val timeLeeway = Date(System.currentTimeMillis()).minusMillis(60000)
            otp == calculateOtp(secret, timeLeeway)
        }
    }

    private fun Date.minusMillis(millis: Int): Date {
        val joda = DateTime(time)
        return Date(joda.minusMillis(millis).millis)
    }
}

interface SecretStore {
    fun secretOf(orgId: String): String
}

class MongoSecretStore : SecretStore, MongoDao("ffc", "secret") {

    override fun secretOf(orgId: String): String {
        var secretKey = getSecret(orgId)

        if (secretKey == null) {
            secretKey = createSecret(orgId)
        }
        return secretKey!!
    }

    internal fun getSecret(orgId: String) =
        dbCollection.find("orgIndex" equal ObjectId(orgId))
            .projection("secretKey" equal 1)
            .firstOrNull()
            ?.get("secretKey") as String?

    internal fun createSecret(orgId: String): String? {
        val secretKey = SecretRandom().nextSecret()
        val doc = Document().apply {
            append("orgIndex", ObjectId(orgId))
            append("secretKey", secretKey)
        }
        dbCollection.insertOne(doc)
        return getSecret(orgId)
    }
}
