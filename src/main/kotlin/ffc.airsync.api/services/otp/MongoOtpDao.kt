package ffc.airsync.api.services.otp

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ignoreException
import org.bson.Document
import org.bson.types.ObjectId
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
        val secretKey = secretString.getSecret()
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
        return otp == (get(orgId, timestamp))
    }
}
