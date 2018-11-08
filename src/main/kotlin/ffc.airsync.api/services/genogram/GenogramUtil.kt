package ffc.airsync.api.services.genogram

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

internal fun List<Person>.processGroupLayer(): Map<Int, ArrayList<Person>> {
    val result = hashMapOf<String, GenogramProcessProperty>()
    calLayer(first(), this, result)
    return groupLayer(result)
}

private typealias GroupXarrayList = HashMap<Int, ArrayList<Person>>

private fun groupLayer(result: HashMap<String, GenogramProcessProperty>): Map<Int, ArrayList<Person>> {
    val group = hashMapOf<Int, ArrayList<Person>>()

    val groupLayerSort = hashMapOf<Int, GroupXarrayList>()

    result.forEach { key, value ->
        val layer = value.layer
        val x = value.x
        if (groupLayerSort[layer] == null)
            groupLayerSort[layer] = hashMapOf()

        if (groupLayerSort[layer]!![x] == null)
            groupLayerSort[layer]!![x] = arrayListOf()

        groupLayerSort[layer]!![x]!!.add(value.person)
    }

    var min = 0

    groupLayerSort.forEach { layer, value ->
        if (layer < min) min = layer
        if (group[layer] == null)
            group[layer] = arrayListOf()

        value.toSortedMap().forEach { key, listPerson ->
            listPerson.forEach { group[layer]!!.add(it) }
        }
    }
    min = min.absoluteValue + 1

    return group.toSortedMap().mapKeys { it.key + min }
}

private fun calLayer(
    person: Person?,
    listPerson: List<Person>,
    result: HashMap<String, GenogramProcessProperty>,
    layer: Int = 0,
    x: Int = 0
) {
    if (person == null) return
    if (person.bundle["genogram"] == "lock") return
    person.bundle["genogram"] = "lock"

    result[person.id] = GenogramProcessProperty(person, layer, x)

    person.fatherId?.let { id ->
        calLayer(listPerson.find { it.id == id }, listPerson, result, layer - 1, x - 1)
    }

    person.motherId?.let { id ->
        calLayer(listPerson.find { it.id == id }, listPerson, result, layer - 1, x)
    }

    person.relationships.filter { it.relate == Married }.forEach { rela ->
        calLayer(listPerson.find { it.id == rela.id }, listPerson, result, layer, x - 1)
    }

    person.relationships.filter { it.relate == Divorced }.forEach { rela ->
        calLayer(listPerson.find { it.id == rela.id }, listPerson, result, layer, x + 1)
    }

    person.relationships.filter { it.relate == Child }.forEach { rela ->
        calLayer(listPerson.find { it.id == rela.id }, listPerson, result, layer + 1, x + 1)
    }

    person.bundle.remove("genogram")
}

private data class GenogramProcessProperty(val person: Person, var layer: Int, var x: Int) {
    val name = person.name
}
