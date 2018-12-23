package ffc.airsync.api.services.notification

import ffc.airsync.api.MongoDbTestRule
import org.amshove.kluent.`should be equal to`
import org.bson.types.ObjectId
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoFirebaseStatusDaoTest {
    lateinit var dao: FirebaseStatusDao

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    val id1 = ObjectId().toHexString()
    val id2 = ObjectId().toHexString()
    val id3 = ObjectId().toHexString()

    @Before
    fun initDb() {
        dao = MongoFirebaseStatusDao(mongo.address.hostString, mongo.address.port)

        dao.insert("5bbd7f5ebc920637b04c7796", id1)
        dao.insert("5bbd7f5ebc920637b04c7796", id2)
        dao.insert("5bbd7f5ebc920637b04c7797", id3)
    }

    @Test
    fun syncCloudFilter() {
        val result = dao.syncData("5bbd7f5ebc920637b04c7796")

        result.count() `should be equal to` 2
        result.first().id `should be equal to` id1
    }

    @Test
    fun confirmSuccess() {
        dao.confirmSuccess("5bbd7f5ebc920637b04c7796", id1)
        val result = dao.syncData("5bbd7f5ebc920637b04c7796")

        result.count() `should be equal to` 1
        result.first().id `should be equal to` id2
    }
}
