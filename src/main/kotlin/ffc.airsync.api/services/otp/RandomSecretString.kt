package ffc.airsync.api.services.otp

import java.security.SecureRandom
import java.util.Locale
import java.util.Objects
import java.util.Random

internal class RandomSecretString constructor(
    length: Int = 64,
    random: Random = SecureRandom(),
    symbols: String = alphanum
) {
    private val random: Random
    private val symbols: CharArray
    private val buf: CharArray

    /**
     * Generate secret string
     */
    fun getSecret(): String {
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
        private const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val lower = upper.toLowerCase(Locale.ROOT)
        private const val digits = "0123456789"
        private val alphanum = upper + lower + digits
    }
}

internal val secretString by lazy { RandomSecretString() }
