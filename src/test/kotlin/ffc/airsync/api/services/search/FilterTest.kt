package ffc.airsync.api.services.search

import ffc.entity.Person
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import org.amshove.kluent.`should be`
import org.joda.time.LocalDate
import org.junit.Test

class FilterTest {

    @Test
    fun filter() {
        val filter = PersonAgeFilter(Query("age", 60, Operator.MORE_THAN))

        filter.filter(person(65)) `should be` true
        filter.filter(person(50)) `should be` false
    }

    @Test
    fun filterElder() {
        val personPool = listOf(
            person(25),
            person(60),
            person(72),
            person(18)
        )

        val querys = QueryExtractor().extract("ผู้สูงอายุ")

        val filted = personPool.filter { p ->
            querys.all { filterFor(it.value)?.filter(p) == true }
        }

        filted.size `should be` 2
    }

    @Test
    fun filterElderWithDM() {
        val personPool = listOf(
            person(25).dm(),
            person(60),
            person(72).dm(),
            person(18)
        )

        val querys = QueryExtractor().extract("ผู้สูงอายุที่เป็นโรคเบาหวาน")

        val filted = personPool.filter { p ->
            querys.all { filterFor(it.value)?.filter(p) == true }
        }

        querys.size `should be` 2
        filted.size `should be` 1
    }

    fun person(age: Int): Person {
        return Person(birthDate = LocalDate.now().minusYears(age))
    }

    fun Person.dm(): Person {
        chronics.add(Chronic(Icd10("เบาหวาน", "E10")))
        return this
    }
}
