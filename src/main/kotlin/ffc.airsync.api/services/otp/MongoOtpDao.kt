package ffc.airsync.api.services.otp

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ignoreException
import org.bson.Document
import org.bson.types.ObjectId
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

    override fun get(orgId: String): String {
        var result = getSecretKey(orgId)

        if (result == null) {
            println("result = null")
            result = createNewSecretKey(orgId)
        } else
            println("have result")

        val secretKey = result!!["secretKey"] as String
        return otpGenerater.getOtp(secretKey)
    }

    private fun createNewSecretKey(orgId: String): Document? {
        val secretKey = secretString.getSecret()
        val doc = Document().apply {
            append("orgIndex", ObjectId(orgId))
            append("secretKey", secretKey)
        }
        dbCollection.insertOne(doc)
        return getSecretKey(orgId)
    }

    private fun getSecretKey(orgId: String) =
        dbCollection.find("orgIndex" equal ObjectId(orgId)).firstOrNull()

    override fun isValid(orgId: String, otp: String): Boolean {
        return otp == (get(orgId))
        // return otpGenerater.isValid(secretKey["secretKey"] as String, otp)
    }
}
