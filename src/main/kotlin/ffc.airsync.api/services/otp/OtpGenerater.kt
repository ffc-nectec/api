package ffc.airsync.api.services.otp

import com.marcelkliemannel.kotlinonetimepassword.HmacAlgorithm
import com.marcelkliemannel.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import com.marcelkliemannel.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import java.util.Date
import java.util.concurrent.TimeUnit

internal class OtpGenerater(
    timeStep: Long = 60,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    codeDigi: Int = 6
) {
    private val config = TimeBasedOneTimePasswordConfig(
        timeStep = timeStep,
        timeStepUnit = timeUnit,
        codeDigits = codeDigi,
        hmacAlgorithm = HmacAlgorithm.SHA512
    )

    fun getOtp(secretKey: String, timestamp: Date = Date(System.currentTimeMillis())): String {
        val otpGenerator = TimeBasedOneTimePasswordGenerator(secretKey.toByteArray(), config)
        return otpGenerator.generate(timestamp)
    }
}
