package ffc.airsync.api.services.personrelationsship

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Father
import ffc.entity.Person.Relate.Married
import ffc.entity.Person.Relate.Mother
import ffc.entity.Person.Relate.Sibling
import ffc.entity.gson.toJson
import ffc.genogram.Family
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test

/**
 *
 */
class GenogramUtil2Test {

    private val `เลือดข้น` = arrayListOf<Person>()
    private val อากง = male("อากง")
    private val `อาม่า` = female("อาม่า")
    private val `ภัสสร` = female("ภัสสร")
    private val `กรกันต์` = male("กรกันต์")
    private val `วิเชียร` = male("วิเชียร")
    private val `น้ำผึ้ง` = female("น้ำผึ้ง")
    private val `อี้` = male("อี้")
    private val `เอิร์น` = male("เอิร์น")
    private val `เต๋า` = male("เต๋า")
    private val `เต้ย` = male("เต้ย")
    private val `เวกัส` = male("เวกัส")
    private val `มาเก๋า` = male("มาเก๋า")

    @Before
    fun setUp() {

        อากง.addRelationship(
            Married to `อาม่า`,
            Child to `ภัสสร`,
            Child to `กรกันต์`
        )

        `อาม่า`.addRelationship(
            Married to อากง,
            Child to `ภัสสร`,
            Child to `กรกันต์`
        )

        `ภัสสร`.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Married to `วิเชียร`,
            Child to `อี้`,
            Child to `เอิร์น`,
            Child to `เต๋า`,
            Child to `เต้ย`,
            Sibling to `กรกันต์`
        )

        `วิเชียร`.addRelationship(
            Married to `ภัสสร`,
            Child to `อี้`,
            Child to `เอิร์น`,
            Child to `เต๋า`,
            Child to `เต้ย`
        )

        `กรกันต์`.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Married to `น้ำผึ้ง`,
            Child to `เวกัส`,
            Child to `มาเก๋า`,
            Sibling to `ภัสสร`
        )

        `น้ำผึ้ง`.addRelationship(
            Married to `กรกันต์`,
            Child to `เวกัส`,
            Child to `มาเก๋า`
        )

        `อี้`.addRelationship(
            Father to `วิเชียร`,
            Mother to `ภัสสร`,
            Sibling to `เอิร์น`,
            Sibling to `เต๋า`,
            Sibling to `เต้ย`
        )

        `เอิร์น`.addRelationship(
            Father to `วิเชียร`,
            Mother to `ภัสสร`,
            Sibling to `อี้`,
            Sibling to `เต๋า`,
            Sibling to `เต้ย`
        )

        `เต๋า`.addRelationship(
            Father to `วิเชียร`,
            Mother to `ภัสสร`,
            Sibling to `อี้`,
            Sibling to `เอิร์น`,
            Sibling to `เต้ย`
        )

        `เต้ย`.addRelationship(
            Father to `วิเชียร`,
            Mother to `ภัสสร`,
            Sibling to `อี้`,
            Sibling to `เอิร์น`,
            Sibling to `เต๋า`
        )

        `เวกัส`.addRelationship(
            Father to `กรกันต์`,
            Mother to `น้ำผึ้ง`,
            Sibling to `มาเก๋า`
        )

        `มาเก๋า`.addRelationship(
            Father to `กรกันต์`,
            Mother to `น้ำผึ้ง`,
            Sibling to `เวกัส`
        )

        `เลือดข้น`.apply {

            add(`เวกัส`)
            add(`เอิร์น`)
            add(อากง)
            add(`อาม่า`)
            add(`วิเชียร`)
            add(`ภัสสร`)
            add(`กรกันต์`)
            add(`เต๋า`)
            add(`เต้ย`)
            add(`มาเก๋า`)
            add(`น้ำผึ้ง`)
            add(`อี้`)
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
        result[2]!!.size `should be equal to` 4
        result[3]!!.size `should be equal to` 6
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
        result.size `should be equal to` 12
    }
}
