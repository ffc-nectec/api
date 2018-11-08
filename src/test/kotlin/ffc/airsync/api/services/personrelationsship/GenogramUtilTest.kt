package ffc.airsync.api.services.personrelationsship

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Divorced
import ffc.entity.Person.Relate.Father
import ffc.entity.Person.Relate.Married
import ffc.entity.Person.Relate.Mother
import ffc.entity.Person.Relate.Sibling
import ffc.entity.gson.toJson
import ffc.genogram.Family
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test

class GenogramUtilTest {

    private val `เลือดข้น` = arrayListOf<Person>()
    private val อากง = male("อากง")
    private val `อาม่า` = female("อาม่า")
    private val `ประเสริฐ` = male("ประเสริฐ")
    private val เมธ = male("เมธ")
    private val `ภัสสร` = female("ภัสสร")
    private val `กรกันต์` = male("กรกันต์")
    private val `มนฤดี` = female("มนฤดี")
    private val `นิภา` = female("นิภา")
    private val `คริส` = female("คริส")
    private val `วิเชียร` = male("วิเชียร")
    private val `น้ำผึ้ง` = female("น้ำผึ้ง")
    private val `พีท` = male("พีท")
    private val `ฉี` = male("ฉี")
    private val `เหม่เหม` = female("เหม่เหม")
    private val `อี้` = male("อี้")
    private val `เอิร์น` = male("เอิร์น")
    private val `เต๋า` = male("เต๋า")
    private val `เต้ย` = male("เต้ย")
    private val `ก๋วยเตี๋ยว` = male("ก๋วยเตี๋ยว")
    private val `เวกัส` = male("เวกัส")
    private val `มาเก๋า` = male("มาเก๋า")

    @Before
    fun setUp() {

        อากง.addRelationship(
            Married to `อาม่า`,
            Child to `ประเสริฐ`,
            Child to เมธ,
            Child to `ภัสสร`,
            Child to `มนฤดี`,
            Child to `กรกันต์`
        )

        `อาม่า`.addRelationship(
            Married to อากง,
            Child to `ประเสริฐ`,
            Child to เมธ,
            Child to `ภัสสร`,
            Child to `มนฤดี`,
            Child to `กรกันต์`
        )

        `ประเสริฐ`.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Divorced to `คริส`,
            Child to `พีท`,
            Married to `นิภา`,
            Child to `ฉี`,
            Sibling to เมธ,
            Sibling to `ภัสสร`,
            Sibling to `มนฤดี`,
            Sibling to `กรกันต์`
        )

        `นิภา`.addRelationship(
            Married to `ประเสริฐ`,
            Child to `ฉี`
        )

        `คริส`.addRelationship(
            Married to `ประเสริฐ`,
            Child to `พีท`
        )

        เมธ.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Child to `เหม่เหม`,
            Sibling to `ประเสริฐ`,
            Sibling to `ภัสสร`,
            Sibling to `มนฤดี`,
            Sibling to `กรกันต์`
        )

        `ภัสสร`.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Married to `วิเชียร`,
            Child to `อี้`,
            Child to `เอิร์น`,
            Child to `เต๋า`,
            Child to `เต้ย`,
            Sibling to `ประเสริฐ`,
            Sibling to `เมธ`,
            Sibling to `มนฤดี`,
            Sibling to `กรกันต์`
        )

        `วิเชียร`.addRelationship(
            Married to `ภัสสร`,
            Child to `อี้`,
            Child to `เอิร์น`,
            Child to `เต๋า`,
            Child to `เต้ย`
        )

        `มนฤดี`.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Child to `ก๋วยเตี๋ยว`
        )

        `กรกันต์`.addRelationship(
            Father to อากง,
            Mother to `อาม่า`,
            Married to `น้ำผึ้ง`,
            Child to `เวกัส`,
            Child to `มาเก๋า`,
            Sibling to `ประเสริฐ`,
            Sibling to `เมธ`,
            Sibling to `ภัสสร`,
            Sibling to `มนฤดี`
        )

        `น้ำผึ้ง`.addRelationship(
            Married to `กรกันต์`,
            Child to `เวกัส`,
            Child to `มาเก๋า`
        )

        `ฉี`.addRelationship(
            Father to `ประเสริฐ`,
            Mother to `นิภา`
        )

        `พีท`.addRelationship(
            Father to `ประเสริฐ`,
            Mother to `คริส`
        )

        `เหม่เหม`.addRelationship(
            Father to เมธ
        )

        `ก๋วยเตี๋ยว`.addRelationship(
            Father to `มนฤดี`
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
            add(`นิภา`)
            add(`คริส`)
            add(`เอิร์น`)
            add(อากง)
            add(`อาม่า`)
            add(`ประเสริฐ`)
            add(เมธ)
            add(`วิเชียร`)
            add(`ภัสสร`)
            add(`กรกันต์`)
            add(`มนฤดี`)
            add(`เต๋า`)
            add(`เต้ย`)
            add(`ก๋วยเตี๋ยว`)
            add(`มาเก๋า`)
            add(`น้ำผึ้ง`)
            add(`พีท`)
            add(`ฉี`)
            add(`เหม่เหม`)
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
        result[2]!!.size `should be equal to` 9
        result[3]!!.size `should be equal to` 10
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
        result.size `should be equal to` 21
    }
}
