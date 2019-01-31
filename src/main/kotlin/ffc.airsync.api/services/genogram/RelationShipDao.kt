package ffc.airsync.api.services.genogram

import ffc.airsync.api.services.Dao
import ffc.entity.Person
import ffc.genogram.GenderLabel
import kotlin.math.absoluteValue

interface GenoGramDao : Dao {
    fun get(orgId: String, personId: String): List<Person.Relationship>
    fun update(orgId: String, personId: String, relation: List<Person.Relationship>): List<Person.Relationship>
    fun collectGenogram(orgId: String, personId: String): List<Person>

    fun removeByOrgId(orgId: String)

    fun insertBlock(
        orgId: String,
        block: Int,
        relation: Map<String, List<Person.Relationship>>
    ): Map<String, List<Person.Relationship>>

    fun removeInsertBlock()

    fun confirmBlock(orgId: String, block: Int)
    fun unConfirmBlock(orgId: String, block: Int)
    fun getBlock(orgId: String, block: Int): Map<String, List<Person.Relationship>>
}

fun Person.buildGeogramPerson(group: List<Person>): ffc.genogram.Person {
    val person = ffc.genogram.Person(
        idCard = id.hashCode().absoluteValue,
        firstname = firstname,
        lastname = lastname,
        gender = if (sex == Person.Sex.FEMALE) GenderLabel.FEMALE else GenderLabel.MALE,
        father = fatherId?.hashCode()?.absoluteValue,
        mother = motherId?.hashCode()?.absoluteValue,
        exHusband = null,
        twin = null,
        exWife = null,
        husband = group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.MALE)
                listOf(it.hashCode().absoluteValue)
            else
                null
        },
        wife = group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.FEMALE)
                listOf(it.hashCode().absoluteValue)
            else
                null
        },
        children = childId.map { it.hashCode().absoluteValue }
    )
    if ((person.children?.isEmpty() != false)) {
        person.children = null
    }

    person.properties = this

    return person
}

val personRelationsShip: GenoGramDao by lazy { MongoRelationsShipDao() }
