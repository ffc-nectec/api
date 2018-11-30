package ffc.airsync.api.services.search

import org.amshove.kluent.`should have value`
import org.junit.Test

class QueryExtractorTest {

    val extractor = QueryExtractor()

    @Test
    fun extract() {
        val query = extractor.extract("ผู้สูงอายุที่เป็นโรคเบาหวาน")

        query `should have value` Query("age", 60, Operator.MORE_THAN)
        query `should have value` Query("dm", true)
    }

    @Test
    fun extract2() {
        val query = extractor.extract("ผู้ป่วยความดันโลหิตสูง")

        query `should have value` Query("ht", true)
    }
}
