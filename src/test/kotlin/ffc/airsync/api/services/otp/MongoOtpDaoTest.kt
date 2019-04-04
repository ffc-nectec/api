package ffc.airsync.api.services.otp

import ffc.airsync.api.MongoDbTestRule
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.amshove.kluent.`should not be in`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoOtpDaoTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: OtpDao

    @Before
    fun initDb() {
        dao = MongoOtpDao(100)
    }

    @Test
    fun get() {
        val otp = dao.get(ORG_ID)
        otp.isNotBlank() `should be equal to` true
    }

    @Test
    fun getNotExpire() {
        val otp = dao.get(ORG_ID)
        otp `should be equal to` dao.get(ORG_ID)
    }

    @Test
    fun getExpire() {
        val otp = dao.get(ORG_ID)
        Thread.sleep(150)
        otp `should not be equal to` dao.get(ORG_ID)
    }

    @Test
    fun validateFail() {
        dao.validateOk(ORG_ID, "333333") `should be equal to` false
    }

    @Test
    fun validateBest() {
        val otp = dao.get(ORG_ID)
        dao.validateOk(ORG_ID, otp) `should be equal to` true
    }

    @Test
    fun validateOutTime() {
        val otp = dao.get(ORG_ID)
        Thread.sleep(150)
        dao.validateOk(ORG_ID, otp) `should be equal to` false
    }

    @Test
    fun forceNextOtp() {
        val check = arrayListOf<String>()
        runBlocking {
            repeat(100) {
                val otp = dao.forceNextOpt(ORG_ID)
                otp `should not be in` check
                check.add(otp)
            }
        }
    }
}
