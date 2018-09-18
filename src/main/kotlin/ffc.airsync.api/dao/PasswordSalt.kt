package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object PasswordSalt {
    private var saltTest = """
uxF3Ocv5eg4BoQBK9MmR
rwPARiCL9ovpr3zmlJlj
kIQnpzRIgEh8WLFNHyy1
ALqs9ES1aQlsc47DlG5f
SbAOMWzMd1T03dyigoHR
7hox2nDJ7tMJRHab5gsy
Ux2VxiCIvJtfPAobOxYW
HazJzQEGdXpmeM2aK6MD
mpOARM2427A6CY14uomK
Cxe9aEkJEFtlLLo6NaNW
yLkbHUfMNDwWeu2BRXuS
m7BHwYSyKGFJdLnq4jJd
sr4QI6aK7g3GCm8vG6Pd
RAtlJZFto0bi9OZta5b4
DLrNTZXXtB3Ci17sepXU
HSYUuw11GJmeuiLKgJYZ
PCHuw2hpoozErKVxEv86
f6zMttthJyQnrDBHGhma
j1nrasD5fg9NxuwkdJq8
ytF2v69RwtGYf7C6ygwD
"""

    var SALT_PASS: String

    init {
        val salt = System.getenv("FFC_SALT")
        if (salt != null) SALT_PASS = salt
        else SALT_PASS = saltTest
    }

    fun getPass(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encoded = digest.digest(("$password$SALT_PASS$password").toByteArray(StandardCharsets.UTF_8))

        val hexString = StringBuffer()
        for (i in 0 until encoded.size) {
            val hex = Integer.toHexString(0xff and encoded[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        printDebug("\t\tSalt pass $hexString")
        return hexString.toString()
    }
}
