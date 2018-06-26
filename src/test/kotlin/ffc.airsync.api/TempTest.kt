package ffc.airsync.api

import ffc.entity.toJson
import me.piruin.geok.geometry.Point
import org.junit.Test

class TempTest {
    @Test
    fun geoOkJson() {
        val point = Point(13.0, 100.0)
        println(point.toJson())
        println(point.toString())
    }
}