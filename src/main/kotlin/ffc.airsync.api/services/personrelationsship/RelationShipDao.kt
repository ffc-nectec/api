package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Person

interface GenoGramDao : Dao {
    fun get(orgId: String, personId: String): List<Person.Relationship>
    fun update(orgId: String, personId: String, relation: List<Person.Relationship>, validate: (person: Person, updateRelation: List<Person.Relationship>) -> Unit = validateRelation): List<Person.Relationship>
}

private val validateRelation: (Person, List<Person.Relationship>) -> Unit =
    { person: Person, updateRelation: List<Person.Relationship> ->
        require(updateRelation.find { it.id == person.id } == null) { "ไม่สามารถมีความสัมพันธ์กับตัวเองได้" }
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

                groupRelation.forEach { relateGroup, value ->
                    require(value.count() == 1) { "พบการใส่ความสัมพันธ์ซ้ำ $relateGroup count ${value.count()}" }

                    when (relateGroup) {
                        Person.Relate.Child ->
                            require(updateRelation.find {
                                it.id == value.first().id && (it.relate == Person.Relate.Mother || it.relate == Person.Relate.Father)
                            } == null) { "ตรวจพบความสัมพันธ์ในครอบครัวแปลก $relateGroup Mother or Father" }
                    }
                }
            }
        }
    }
val personRelationsShip: GenoGramDao by lazy { MongoRelationsShipDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
