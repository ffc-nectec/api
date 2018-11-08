package ffc.airsync.api.services.personrelationsship

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Divorced
import ffc.entity.Person.Relate.Married
import kotlin.math.absoluteValue

internal fun Map<Int, ArrayList<Person>>.toList(): List<Person> {
    val list = arrayListOf<Person>()
    forEach { key, value ->
        value.forEach {
            list.add(it)
        }
    }
    return list
}

internal fun List<Person>.deep(): Map<Int, ArrayList<Person>> {
    val result = hashMapOf<String, GenogramProcessProperty>()
    calDeep(first(), this, result)
    return groupDeep(result)
}

private typealias GroupXarrayList = HashMap<Int, ArrayList<Person>>

private fun groupDeep(result: HashMap<String, GenogramProcessProperty>): Map<Int, ArrayList<Person>> {
    val groupDeep = hashMapOf<Int, ArrayList<Person>>()

    val groupSuperDeep = hashMapOf<Int, GroupXarrayList>()

    result.forEach { key, value ->
        val deep = value.deep
        val x = value.x
        if (groupSuperDeep[deep] == null)
            groupSuperDeep[deep] = hashMapOf()

        if (groupSuperDeep[deep]!![x] == null)
            groupSuperDeep[deep]!![x] = arrayListOf()

        groupSuperDeep[deep]!![x]!!.add(value.person)
    }

    var min = 0

    groupSuperDeep.forEach { deep, value ->
        if (deep < min) min = deep
        if (groupDeep[deep] == null)
            groupDeep[deep] = arrayListOf()

        value.toSortedMap().forEach { key, listPerson ->
            listPerson.forEach { groupDeep[deep]!!.add(it) }
        }
    }
    min = min.absoluteValue + 1

    return groupDeep.toSortedMap().mapKeys { it.key + min }
}

private fun calDeep(
    person: Person?,
    listPerson: List<Person>,
    result: HashMap<String, GenogramProcessProperty>,
    deep: Int = 0,
    x: Int = 0
) {
    if (person == null) return
    if (person.bundle["genogram"] == "lock") return
    person.bundle["genogram"] = "lock"

    result[person.id] = GenogramProcessProperty(person, deep, x)

    person.fatherId?.let { id ->
        calDeep(listPerson.find { it.id == id }, listPerson, result, deep - 1, x - 1)
    }

    person.motherId?.let { id ->
        calDeep(listPerson.find { it.id == id }, listPerson, result, deep - 1, x)
    }

    person.relationships.filter { it.relate == Married }.forEach { rela ->
        calDeep(listPerson.find { it.id == rela.id }, listPerson, result, deep, x - 1)
    }

    person.relationships.filter { it.relate == Divorced }.forEach { rela ->
        calDeep(listPerson.find { it.id == rela.id }, listPerson, result, deep, x + 1)
    }

    person.relationships.filter { it.relate == Child }.forEach { rela ->
        calDeep(listPerson.find { it.id == rela.id }, listPerson, result, deep + 1, x + 1)
    }

    person.bundle.remove("genogram")
}

private data class GenogramProcessProperty(val person: Person, var deep: Int, var x: Int) {
    val name = person.name
}
