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
import java.util.Date
import java.util.concurrent.TimeUnit

interface Otp {

    fun generate(): String

    fun verify(otp: String): Boolean
}

class OrgTimebaseOtp(val orgId: String) : Otp {

    private val config = TimeBasedOneTimePasswordConfig(
        timeStep = 60,
        timeStepUnit = TimeUnit.SECONDS,
        codeDigits = 6,
        hmacAlgorithm = HmacAlgorithm.SHA512
    )

    val secretStore: SecretStore = MongoSecretStore()//TODO init

    override fun generate(): String {
        val secret = secretStore.secretOf(orgId)
        val otpGenerator = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
        return otpGenerator.generate()
    }

    internal fun generateLeeway(timestamp: Date): String {
        //implement
        return "leeway otp"
    }

    override fun verify(otp: String): Boolean {
        if (otp == generate())
            return true
        //TODO check last
        return otp == generateLeeway(Date(System.currentTimeMillis()))
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

