package ffc.airsync.api

import ffc.entity.Entity
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should equal`
import org.bson.Document
import org.junit.Ignore
import org.junit.Test

class JsonTest {
    @Test
    fun geokPointToJson() {
        val point = Point(13.0, 100.0)

        with(point) {
            toJson() `should equal` """{"type":"Point","coordinates":[100.0,13.0]}"""
            toString() `should equal` "Point(coordinates=13.0, 100.0)"
        }
    }

    @Test
    @Ignore("บน Heoroku เวลาไม่ตรงกัน +7")
    fun timestampFormatAfterBsonConvertShouldNotChange() {
        val entity = """
{
  "id": "d939jdndddd",
  "timestamp": "2018-06-28T14:19:29.645+07:00"
}
        """.parseTo<Entity>()
        println("Gson = ${entity.toJson()}")

        val bsonDoc = Document.parse(entity.toJson())

        bsonDoc.toJson() `should contain` "2018-06-28T14:19:29.645+07:00"
    }
}
