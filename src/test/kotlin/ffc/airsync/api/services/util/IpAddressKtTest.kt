package ffc.airsync.api.services.util

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class IpAddressKtTest {
    val ip = "203.185.134.231, 172.68.6.45"
    @Test
    fun getFirstIp() {
        getFirstIp(ip) `should be equal to` "203.185.134.231"
    }
}
