package ffc.airsync.api.services.util

import org.amshove.kluent.`should be`
import org.junit.Test

class CollectionsTest {

    @Test
    fun containsSome() {
        val list = listOf(1, 2, 4, 8, 16)

        list.containsSome(1, 2) `should be` true
        list.containsSome(3, 4) `should be` true
        list.containsSome(5, 6, 7) `should be` false
    }
}
