package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Divorced
import ffc.entity.Person.Relate.Father
import ffc.entity.Person.Relate.LegallySeperated
import ffc.entity.Person.Relate.Married
import ffc.entity.Person.Relate.Mother
import ffc.entity.Person.Relate.Seperated

interface GenoGramDao : Dao {
    fun get(orgId: String, personId: String): List<Person.Relationship>
    fun update(orgId: String, personId: String, relation: List<Person.Relationship>, validate: (personId: String, updateRelation: List<Person.Relationship>) -> Unit = validateRelation): List<Person.Relationship>
}

private val validateRelation: (String, List<Person.Relationship>) -> Unit =
    { person: String, updateRelation: List<Person.Relationship> ->
        require(updateRelation.find { it.id == person } == null) { "ไม่สามารถมีความสัมพันธ์กับตัวเองได้" }
        val groupPerson: HashMap<String, ArrayList<Person.Relationship>> = hashMapOf()
        updateRelation.forEach {
            if (groupPerson[it.id] == null) groupPerson[it.id] = arrayListOf()
            groupPerson[it.id]!!.add(it)
        }

        groupPerson.forEach { personId, relation ->
            if (relation.count() > 1) {
                val groupRelation: HashMap<Person.Relate, ArrayList<Person.Relationship>> = hashMapOf()
                relation.forEach {
                    if (groupRelation[it.relate] == null) groupRelation[it.relate] = arrayListOf()
                    groupRelation[it.relate]!!.add(it)
                }

                groupRelation.forEach { relateGroup, personRelation ->
                    require(personRelation.count() == 1) { "พบการใส่ความสัมพันธ์ซ้ำ $relateGroup count ${personRelation.count()}" }
                    conditionRelationShip(updateRelation, personRelation, relateGroup)
                }
            }
        }
    }

private fun conditionRelationShip(updateRelation: List<Person.Relationship>, personRelation: java.util.ArrayList<Person.Relationship>, relateGroup: Person.Relate) {
    val personGroupRelation = updateRelation.filter { it.id == personRelation.first().id }
    when (relateGroup) {
        Child -> {
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Mother) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Mother is Child" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Father) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Father is Child" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Married) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Married with child" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Seperated) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Separated with child" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` LegallySeperated) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Legally Separated with child" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Divorced) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Divorced with child" }
        }
        Married -> {
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Mother) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Married with mother" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Father) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Married with father" }

            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Seperated) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup You have to choose Married, Divorced, Separated, LegallySeperated" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` LegallySeperated) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup You have to choose Married, Divorced, Separated, LegallySeperated" }
            require(personGroupRelation `ไม่มีความสัมพันธ์เป็น` Divorced) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup You have to choose Married, Divorced, Separated, LegallySeperated" }
        }
    }
}

private infix fun List<Person.Relationship>.`ไม่มีความสัมพันธ์เป็น`(relation: Person.Relate): Boolean =
    find { it.relate == relation } == null

val personRelationsShip: GenoGramDao by lazy { MongoRelationsShipDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
