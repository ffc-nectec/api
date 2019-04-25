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

package ffc.airsync.api.services.otp

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.security.SecretRandom
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ignoreException
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import java.util.Date
import java.util.concurrent.TimeUnit

class MongoOtpDao(
    timeStep: Long = 60,
    timeUnit: TimeUnit = TimeUnit.SECONDS
) : OtpDao, MongoDao("ffc", "otp") {

    private val otpGenerater by lazy {
        OtpGenerater(
            timeStep = timeStep,
            timeUnit = timeUnit
        )
    }

    init {
        ignoreException {
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(true))
        }
    }

    override fun get(orgId: String, timestamp: Date): String {
        var secretKey = secretKeyOrg(orgId)

        if (secretKey == null) {
            secretKey = createNewSecretKey(orgId)
        }

        return otpGenerater.getOtp(secretKey!!, timestamp)
    }

    private fun createNewSecretKey(orgId: String): String? {
        val secretKey = SecretRandom().nextSecret()
        val doc = Document().apply {
            append("orgIndex", ObjectId(orgId))
            append("secretKey", secretKey)
        }
        dbCollection.insertOne(doc)
        return secretKeyOrg(orgId)
    }

    private fun secretKeyOrg(orgId: String) =
        dbCollection.find("orgIndex" equal ObjectId(orgId))
            .projection("secretKey" equal 1)
            .firstOrNull()
            ?.get("secretKey") as String?

    override fun isValid(orgId: String, otp: String, timestamp: Date): Boolean {
        val checkCurrentTime = otp == (get(orgId, timestamp))
        val checkPastTime = otp == (get(orgId, timestamp.minusMillis(60000)))
        return checkCurrentTime || checkPastTime
    }

    private fun Date.minusMillis(millis: Int): Date {
        val joda = DateTime(time)
        return Date(joda.minusMillis(millis).millis)
    }
}
