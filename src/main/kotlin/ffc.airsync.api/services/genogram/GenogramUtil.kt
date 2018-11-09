package ffc.airsync.api.services.genogram

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Divorced
import ffc.entity.Person.Relate.Father
import ffc.entity.Person.Relate.Married
import ffc.entity.Person.Relate.Mother
import ffc.entity.Person.Sex.FEMALE
import ffc.entity.Person.Sex.MALE
import kotlin.math.absoluteValue

/**
 * แปลง Map ที่มีข้อมูลชั้น Layer ของ Person ให้เป็น List
 */
internal fun Map<Int, ArrayList<Person>>.toList(): List<Person> {
    val list = arrayListOf<Person>()
    forEach { key, value ->
        value.forEach {
            list.add(it)
        }
    }
    return list
}

/**
 * หาชั้นของ Layer ที่จะนำไปใช้วาด Genogram
 * @param this ก้อน Person ที่มีความสัมพันธ์ ถึงกัน
 * @return Map ที่มีข้อมูลชั้น Layer ของ Person
 */
internal fun List<Person>.processGroupLayer(): Map<Int, ArrayList<Person>> {
    val result = hashMapOf<String, GenogramProcessProperty>()
    calLayer(first(), this.addDummy(), result)
    return groupLayer(result)
}

private typealias GroupXarrayList = HashMap<Int, ArrayList<Person>>

/**
 * เรียงข้อมูลตาม group ของ layer
 */
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

/**
 * คำนวนหาชั้นของ Layer
 */
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

/**
 * ในกรณีที่ มีลูก แต่ไม่สามารถหาคู่ ในฐานข้อมูลได้
 * จะสร้างคนว่างๆ ให้มาเป็นคู่แทน
 * เนื่องจาก Library ในการวาดไม่ support ลูกติด
 * ถ้ามีลูก ต้องมีคู่
 */
private fun List<Person>.addDummy(): List<Person> {
    val list = arrayListOf<Person>()
    list.addAll(this)

    forEach {

        val childGroup = it.relationships.filter { it.relate == Child }
        val dummyRelation = if (it.sex == FEMALE) Father else Mother
        if (childGroup.isNotEmpty()) {
            val twin = it.relationships.filter { it.relate == Married || it.relate == Divorced }
            val dummyPerson = ffc.entity.Person()
            if (twin.isEmpty()) {
                list.add(dummyPerson)

                it.addRelationship(Pair(Married, dummyPerson))
                dummyPerson.addRelationship(Pair(Married, it))
                dummyPerson.sex = if (it.sex == FEMALE) MALE else FEMALE
                childGroup.forEach { child ->
                    val childPerson = list.find { child.id == it.id }!!
                    addChildPerson(childPerson, dummyRelation, dummyPerson)
                }
            } else {
                childGroup.forEach { child ->

                    val childPerson = list.find { child.id == it.id }!!
                    val childParent = childPerson.relationships?.find { it.relate == dummyRelation }

                    if (childParent == null) {
                        addChildPerson(childPerson, dummyRelation, dummyPerson)
                    }
                }
            }
        }
    }
    return list
}

private fun addChildPerson(
    childPerson: Person,
    dummyRelation: Person.Relate,
    dummyPerson: Person
) {
    childPerson.addRelationship(Pair(dummyRelation, dummyPerson))
    dummyPerson.addRelationship(Pair(Child, childPerson))
}
