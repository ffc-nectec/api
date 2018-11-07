package ffc.airsync.api.services.personrelationsship

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Divorced
import ffc.entity.Person.Relate.Married
import kotlin.math.absoluteValue

internal fun List<Person>.deep(): Map<Int, ArrayList<Person>> {
    val result = hashMapOf<String, GenogramProcessProperty>()
    calDeep(first(), this, result)
    return groupDeep(result)
}

private fun groupDeep(result: HashMap<String, GenogramProcessProperty>): Map<Int, ArrayList<Person>> {
    val groupDeep = hashMapOf<Int, ArrayList<Person>>()

    result.forEach { key, value ->
        if (groupDeep[value.deep] == null)
            groupDeep[value.deep] = arrayListOf()

        groupDeep[value.deep]!!.add(value.person)
    }

    // start 1 2 3 4 5
    var min = 0
    groupDeep.forEach { key, value ->
        if (key < min) min = key
    }
    min = min.absoluteValue + 1

    return groupDeep.mapKeys { it.key + min }
}

private fun calDeep(
    person: Person?,
    listPerson: List<Person>,
    result: HashMap<String, GenogramProcessProperty>,
    deep: Int = 0
) {
    if (person == null) return
    if (person.bundle["genogram"] == "lock") return
    person.bundle["genogram"] = "lock"

    result[person.id] = GenogramProcessProperty(person, deep)

    person.fatherId?.let { id ->
        calDeep(listPerson.find { it.id == id }, listPerson, result, deep - 1)
    }

    person.motherId?.let { id ->
        calDeep(listPerson.find { it.id == id }, listPerson, result, deep - 1)
    }

    person.relationships.filter { it.relate == Married }.forEach { rela ->
        calDeep(listPerson.find { it.id == rela.id }, listPerson, result, deep)
    }

    person.relationships.filter { it.relate == Divorced }.forEach { rela ->
        calDeep(listPerson.find { it.id == rela.id }, listPerson, result, deep)
    }

    person.relationships.filter { it.relate == Child }.forEach { rela ->
        calDeep(listPerson.find { it.id == rela.id }, listPerson, result, deep + 1)
    }

    person.bundle.remove("genogram")
}

internal data class GenogramProcessProperty(val person: Person, var deep: Int) {
    val name = person.name
}
