package ffc.airsync.api.services.genogram

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Father
import ffc.entity.Person.Relate.Married
import ffc.entity.Person.Relate.Mother
import ffc.entity.gson.toJson
import ffc.genogram.Family
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test

class GenogramUtilGenDummyTinyTest {

    private val `เลือดข้น` = arrayListOf<Person>()
    private val อากง = male("อากง")
    private val `อาม่า` = female("อาม่า")
    private val เมธ = male("เมธ")
    private val `เหม่เหม` = female("เหม่เหม")

    @Before
    fun setUp() {

        อากง.addRelationship(
            Married to `อาม่า`,
            Child to เมธ
        )

        `อาม่า`.addRelationship(
            Married to อากง,
            Child to เมธ
        )

        เมธ.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Child to `เหม่เหม`
        )

        `เหม่เหม`.addRelationship(
            Father to เมธ
        )

        `เลือดข้น`.apply {

            add(อากง)
            add(เมธ)
            add(`เหม่เหม`)
            add(`อาม่า`)
        }
    }

    fun male(name: String, key: String = name): Person {
        return Person("{{$key}}").apply {
            firstname = name
            lastname = "จิระอนันต์"
            sex = Person.Sex.MALE
            houseId = "{{house_id_for_geno}}"
        }
    }

    fun female(name: String, key: String = name): Person {
        return Person("{{$key}}").apply {
            firstname = name
            lastname = "จิระอนันต์"
            sex = Person.Sex.FEMALE
            houseId = "{{house_id_for_geno}}"
        }
    }

    @Test
    fun deep() {
        val result = `เลือดข้น`.processGroupLayer()

        result.size `should be equal to` 3
        result[1]!!.size `should be equal to` 2
        result[2]!!.size `should be equal to` 2
        result[3]!!.size `should be equal to` 1
    }

    @Test
    fun toList() {
        val result = `เลือดข้น`.processGroupLayer().toList()

        val member: List<ffc.genogram.Person> = result.map {
            it.buildGeogramPerson(result)
        }

        val family = Family(0, result.first().lastname, member)

        family.bloodFamily?.forEach { blood ->
            val person = member.find { it.idCard == blood }!!
            val linkName = person.linkedStack!!.map { link -> member.find { it.idCard == link }!!.firstname }
            println("${person.firstname} link $linkName")
        }
        println(family.toJson())
        result.size `should be equal to` 5
    }
}
