package ffc.airsync.api.services.personrelationsship

import ffc.entity.Person
import ffc.entity.Person.Relate.Child
import ffc.entity.Person.Relate.Divorced
import ffc.entity.Person.Relate.Father
import ffc.entity.Person.Relate.Married
import ffc.entity.Person.Relate.Mother
import ffc.entity.Person.Relate.Sibling
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
            add(`ภัสสร`)
            add(`กรกันต์`)
            add(`มนฤดี`)
            add(`นิภา`)
            add(`คริส`)
            add(อากง)
            add(`อาม่า`)
            add(`ประเสริฐ`)
            add(เมธ)
            add(`วิเชียร`)
            add(`น้ำผึ้ง`)
            add(`พีท`)
            add(`ฉี`)
            add(`เหม่เหม`)
            add(`อี้`)
            add(`เอิร์น`)
            add(`เต๋า`)
            add(`เต้ย`)
            add(`ก๋วยเตี๋ยว`)
            add(`เวกัส`)
            add(`มาเก๋า`)
        }
    }

    fun male(name: String): Person {
        return Person().apply {
            firstname = name
            lastname = "จิระอนันต์"
            sex = Person.Sex.MALE
        }
    }

    fun female(name: String): Person {
        return Person().apply {
            firstname = name
            lastname = "จิระอนันต์"
            sex = Person.Sex.FEMALE
        }
    }

    @Test
    fun deep() {
        val result = `เลือดข้น`.deep()

        result.size `should be equal to` 3
        result[1]!!.size `should be equal to` 2
        result[2]!!.size `should be equal to` 9
        result[3]!!.size `should be equal to` 10
    }
}
