package ffc.airsync.api.services.otp

import ffc.airsync.api.MongoDbTestRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class MongoOtpDaoTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    private val ORG_ID2 = "5bbd7f5ebc920637b04c7799"
    lateinit var dao: OtpDao

    @Before
    fun initDb() {
        dao = MongoOtpDao(10, TimeUnit.SECONDS)
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
        Thread.sleep(5000)
        dao.isValid(ORG_ID, otp) `should be equal to` true
    }

    @Test
    fun getExpire() {
        val otp = dao.get(ORG_ID)
        Thread.sleep(15000)
        dao.isValid(ORG_ID, otp) `should be equal to` false
    }

    @Test
    fun validateFail() {
        dao.isValid(ORG_ID, "333333") `should be equal to` false
    }

    @Test
    fun doNotAcceptOtherOrganizations() {
        val otp1 = dao.get(ORG_ID)
        val otp2 = dao.get(ORG_ID2)

        dao.isValid(ORG_ID, otp2) `should be equal to` false
        dao.isValid(ORG_ID2, otp1) `should be equal to` false
    }

    @Test
    fun acceptMyOrganizations() {
        val otp1 = dao.get(ORG_ID)
        val otp2 = dao.get(ORG_ID2)

        dao.isValid(ORG_ID, otp1) `should be equal to` true
        dao.isValid(ORG_ID2, otp2) `should be equal to` true
    }

    @Test
    fun get2OtpButAcceptOtp1_Otp1NotExpire() {
        val otpFirst = dao.get(ORG_ID)
        dao.get(ORG_ID2)
        runBlocking {
            delay(5000)
        }
        val otpSecount = dao.get(ORG_ID)
        println("otp1 $otpFirst otp2 $otpSecount")
        dao.isValid(ORG_ID, otpFirst) `should be equal to` true
        otpFirst `should equal` otpSecount
    }
}
