package ffc.airsync.api.services.otp

import ffc.airsync.api.MongoDbTestRule
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class MongoOtpDaoTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: OtpDao

    @Before
    fun initDb() {
        dao = MongoOtpDao(100, TimeUnit.MILLISECONDS)
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
        val otp = dao.get(ORG_ID)
        dao.isValid(ORG_ID, otp) `should be equal to` true
    }

    @Test
    fun getExpire() {
        val otp = dao.get(ORG_ID)
        Thread.sleep(150)
        dao.isValid(ORG_ID, otp) `should be equal to` false
    }

    @Test
    fun validateFail() {
        dao.isValid(ORG_ID, "333333") `should be equal to` false
    }
}
