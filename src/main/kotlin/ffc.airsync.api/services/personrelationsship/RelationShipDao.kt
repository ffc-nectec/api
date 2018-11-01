package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Person
import ffc.genogram.GenderLabel

interface GenoGramDao : Dao {
    fun get(orgId: String, personId: String): List<Person.Relationship>
    fun update(orgId: String, personId: String, relation: List<Person.Relationship>): List<Person.Relationship>
    fun collectGenogram(orgId: String, personId: String, skip: List<Person> = arrayListOf()): List<Person>
}

fun Person.buildGeogramPerson(group: List<Person>): ffc.genogram.Person {
    val person = ffc.genogram.Person(
        id.hashCode().toLong(),
        firstname, lastname,
        if (sex == Person.Sex.FEMALE) GenderLabel.FEMALE else GenderLabel.MALE,
        fatherId?.hashCode()?.toLong(),
        motherId?.hashCode()?.toLong(),
        arrayListOf(),
        arrayListOf(),
        arrayListOf(),
        group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.MALE)
                listOf(it.hashCode())
            else
                listOf()
        },
        group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.FEMALE)
                listOf(it.hashCode())
            else
                listOf()
        },
        childId.map { it.hashCode() },
        null
    )

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

fun List<Person>.buildBloodFamily(): List<Int> = map { it.id.hashCode() }

val personRelationsShip: GenoGramDao by lazy { MongoRelationsShipDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
