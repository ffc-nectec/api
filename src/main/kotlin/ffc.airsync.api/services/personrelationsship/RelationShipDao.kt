package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Person
import ffc.genogram.GenderLabel

interface GenoGramDao : Dao {
    fun get(orgId: String, personId: String): List<Person.Relationship>
    fun update(orgId: String, personId: String, relation: List<Person.Relationship>): List<Person.Relationship>
    fun collectGenogram(orgId: String, personId: String): List<Person>
}

fun Person.buildGeogramPerson(group: List<Person>): ffc.genogram.Person {
    val person = ffc.genogram.Person(
        idCard = id.hashCode().toLong(),
        firstname = firstname,
        lastname = lastname,
        gender = if (sex == Person.Sex.FEMALE) GenderLabel.FEMALE else GenderLabel.MALE,
        father = fatherId?.hashCode()?.toLong(),
        mother = motherId?.hashCode()?.toLong(),
        exHusband = null,
        twin = null,
        exWife = null,
        husband = group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.MALE)
                listOf(it.hashCode())
            else
                null
        },
        wife = group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.FEMALE)
                listOf(it.hashCode())
            else
                null
        },
        children = childId.map { it.hashCode() },
        linkedStack = null
    )
    if ((person.children?.isEmpty() != false)) {
        person.children = null
    }

    person.properties = this

    person.linkedStack = arrayListOf<Int>().apply {
        person.mother?.let {
            add(it.toInt())
        }
        person.father?.let {
            add(it.toInt())
        }
        person.twin?.let {
            it.forEach { add(it) }
        }
        person.exHusband?.let {
            it.forEach { add(it) }
        }
        person.exWife?.let {
            it.forEach { add(it) }
        }
        person.husband?.let {
            it.forEach { add(it) }
        }
        person.wife?.let {
            it.forEach { add(it) }
        }
        person.children?.let {
            it.forEach { add(it) }
        }
    }
    return person
}

val personRelationsShip: GenoGramDao by lazy { MongoRelationsShipDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
