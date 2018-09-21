package ffc.airsync.api

import ffc.entity.User
import org.amshove.kluent.`should be`
import org.junit.Test

class RoleTest {
    @Test
    fun role() {
        val role = User.Role.valueOf("ADMIN")

        role `should be` User.Role.ADMIN
    }
}
