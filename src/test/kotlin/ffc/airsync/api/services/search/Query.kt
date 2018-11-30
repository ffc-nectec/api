package ffc.airsync.api.services.search

import org.amshove.kluent.`should be`
import org.junit.Test

class QueryTest {

    @Test
    fun compareTo() {
        val q = Query("age", 60, Operator.MORE_THAN)

        q.compareTo { 60 } `should be` true
        q.compareTo { 65 } `should be` true
        q.compareTo { 59 } `should be` false
        q.compareTo { -1 } `should be` false
    }

    @Test
    fun compareToBoolean() {
        val q = Query("ht", true)

        q.compareTo { Elder("E10").icd10.startsWith("E1") } `should be` true
        q.compareTo { Elder("I10").icd10.startsWith("E1") } `should be` false
    }

    class Elder(val icd10: String)
}
