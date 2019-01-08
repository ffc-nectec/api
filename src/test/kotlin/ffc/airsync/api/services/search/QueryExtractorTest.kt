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

    @Test
    fun male() {
        val query = extractor.extract("เบาหวานผู้ชายอายุ 50")

        query `should have value` Query("male", true)
    }

    @Test
    fun female() {
        val query = extractor.extract("ผู้หญิงที่เป็นความดันสูง")

        query `should have value` Query("female", true)
    }

    @Test
    fun age() {
        val query = extractor.extract("ผู้หญิงอายุ 24 ปี ที่เป็นความดันสูง")

        query `should have value` Query("age", 24, Operator.EQAUL)
    }

    @Test
    fun ageMore() {
        val query = extractor.extract("ผู้หญิงอายุมากกว่า 24 ปี ที่เป็นความดันสูง")

        query `should have value` Query("age", 24, Operator.MORE_THAN)
    }

    @Test
    fun ageLess() {
        val query = extractor.extract("ผู้หญิงอายุน้อยกว่า 24 ปี ที่เป็นความดันสูง")

        query `should have value` Query("age", 24, Operator.LESS_THEN)
    }

    @Test
    fun ageBetween() {
        val query = extractor.extract("อายุ 20 ถึง 60 ปี")

        query `should have value` Query("agebetween", listOf(20, 60), Operator.EQAUL)
    }

    @Test
    fun ageMoreFemale() {
        val query = extractor.extract("อายุ 65 ขึ้นไป")

        query `should have value` Query("age", 65, Operator.MORE_THAN)
    }

    @Test
    fun ageMore2Female() {
        val query = extractor.extract("อายุ 65 ปีขึ้นไป")

        query `should have value` Query("age", 65, Operator.MORE_THAN)
    }
}
