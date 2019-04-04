package ffc.airsync.api.services.otp

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ignoreException
import org.bson.BsonDateTime
import org.bson.BsonTimestamp
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class MongoOtpDao(private val expireMilli: Int = 30000) : OtpDao, MongoDao("ffc", "otppp") {
    init {
        ignoreException {
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(true))
        }
        ignoreException {
            dbCollection.createIndex(Indexes.ascending("MongoCreated"), IndexOptions().expireAfter(1L, TimeUnit.DAYS))
        }
    }

    private val expireOtp get() = DateTime.now().minusMillis(expireMilli).millis
    private val randomOtp = RandomOtp()

    override fun get(orgId: String): String {
        var otp = findOtp(orgId)
        if (otp == null) {
            otp = createOtp(orgId)
        }

        val dateTime = otp["Timestamp"] as BsonTimestamp
        if (dateTime.value < expireOtp) {
            otp = createOtp(orgId)
        }

        return otp["otp"] as String
    }

    private fun findOtp(orgId: String) =
        dbCollection.find("orgIndex" equal ObjectId(orgId)).firstOrNull()

    private fun createOtp(orgId: String): Document {
        val otpDoc = Document().apply {
            append("orgIndex", ObjectId(orgId))
            append("otp", randomOtp.nextOtp())
            val now = DateTime.now().millis
            append("MongoCreated", BsonDateTime(now))
            append("Timestamp", BsonTimestamp(now))
        }
        try {
            dbCollection.insertOne(otpDoc)
        } catch (ex: com.mongodb.MongoWriteException) {
            dbCollection.deleteOne("orgIndex" equal ObjectId(orgId))
            dbCollection.insertOne(otpDoc)
        }
        return findOtp(orgId)!!
    }

    override fun forceNextOpt(orgId: String): String {
        return createOtp(orgId)["otp"] as String
    }

    override fun validateOk(orgId: String, otp: String): Boolean {
        return get(orgId) == otp
    }
}
