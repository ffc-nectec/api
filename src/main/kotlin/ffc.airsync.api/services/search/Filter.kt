package ffc.airsync.api.services.search

import ffc.entity.Person
import ffc.entity.healthcare.Disease
import ffc.entity.healthcare.Icd10
import ffc.entity.healthcare.analyze.HealthAnalyzer
import ffc.entity.healthcare.analyze.HealthIssue

interface Filter {

    fun filter(obj: Any): Boolean
}

fun filterFor(query: Query<*>): Filter? {
    return when (query.key) {
        "age" -> PersonAgeFilter(query as Query<Int>)
        "ht" -> PersonHtFilter(query as Query<Boolean>)
        "dm" -> PersonDmFilter(query as Query<Boolean>)
        else -> null
    }
}

class PersonAgeFilter(val query: Query<Int>) : Filter {
    override fun filter(obj: Any): Boolean {
        return when (obj) {
            is Person -> query.compareTo { obj.age ?: 0 }
            else -> false
        }
    }
}

class PersonHtFilter(val query: Query<Boolean>) : Filter {

    override fun filter(obj: Any): Boolean {
        return when (obj) {
            is Person -> query.compareTo {
                val analyzer = obj.bundle["analyze"] as HealthAnalyzer?
                if (analyzer != null) {
                    analyzer.result[HealthIssue.Issue.HT]?.haveIssue == true
                } else {
                    obj.chronics.firstOrNull {
                        it.disease.isHt
                    } != null
                }
            }
            else -> false
        }
    }

    val Disease.isHt: Boolean
        get() {
            if (this is Icd10)
                return icd10.contains(Regex("^[iI]1[0-5]"))
            else
                return false
        }
}

class PersonDmFilter(val query: Query<Boolean>) : Filter {

    override fun filter(obj: Any): Boolean {
        return when (obj) {
            is Person -> query.compareTo {
                val analyzer = obj.bundle["analyze"] as HealthAnalyzer?
                if (analyzer != null) {
                    analyzer.result[HealthIssue.Issue.DM]?.haveIssue == true
                } else {
                    obj.chronics.firstOrNull { it.disease.isDm } != null
                }
            }
            else -> false
        }
    }

    val Disease.isDm: Boolean
        get() {
            if (this is Icd10)
                return icd10.contains(Regex("^[eE]1[0-4]"))
            else
                return false
        }
}
