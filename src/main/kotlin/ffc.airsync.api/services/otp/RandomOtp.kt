package ffc.airsync.api.services.otp

import java.security.SecureRandom
import java.util.Objects
import java.util.Random

internal class RandomOtp constructor(length: Int = 6, random: Random = SecureRandom(), symbols: String = alphanum) {
    private val random: Random
    private val symbols: CharArray
    private val buf: CharArray

    /**
     * Generate otp string
     */
    fun nextOtp(): String {
        for (idx in buf.indices)
            buf[idx] = symbols[random.nextInt(symbols.size)]
        return String(buf)
    }

    init {
        if (length < 1) throw IllegalArgumentException()
        if (symbols.length < 2) throw IllegalArgumentException()
        this.random = Objects.requireNonNull(random)
        this.symbols = symbols.toCharArray()
        this.buf = CharArray(length)
    }

    companion object {
        private val alphanum = "0123456789"
    }
}

internal val randomOtp = RandomOtp()
