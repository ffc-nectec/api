/*
 * Copyright (c) 2019 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ffc.airsync.api.services.house

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.Person
import ffc.entity.ThaiCitizenId
import ffc.entity.ThaiHouseholdId
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import ffc.entity.place.House
import ffc.entity.update
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoHouseDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    val ORG_ID = "87543432abcf432123456785"
    lateinit var dao: HouseDao
    lateinit var maxHouse: House
    lateinit var someHouse: House

    @Before
    fun initDb() {
        dao = MongoHouseDao()

        maxHouse = dao.insert(ORG_ID, createHouse("12348764532", "999/888").apply {
            allowUserId = mutableListOf("12345678")
        })
        someHouse = dao.insert(ORG_ID, createHouse("11111111111", "888/777"))
    }

    fun createHouse(identity: String, no: String): House {
        return House().apply {
            this.identity = ThaiHouseholdId(identity)
            this.no = no
            road = "สาธร"
            villageName = "ลัดดา"
            location = Point(13.123321, 113.7765654)
            people.add(createPerson("1234544323423", "นาย มั่นคง มั่งคั่ง"))
            people.add(createPerson("8909877615243", "นางสาว สวย น่ารัก"))
        }
    }

    private fun createPerson(idCard: String, stringName: String): Person {
        val nameStruct = stringName.split(" ")
        return Person().apply {
            identities.add(ThaiCitizenId(idCard))
            prename = nameStruct[0]
            firstname = nameStruct[1]
            lastname = nameStruct[2]
            sex = if (nameStruct[0].trim() == "นาย") Person.Sex.MALE else Person.Sex.FEMALE
            birthDate = LocalDate.now().minusMonths(240)
            chronics.add(Chronic(Icd10("fair", "dx001")))
        }
    }

    @Test
    fun findByHouseId() {
        val maxHouseFind = dao.find(ORG_ID, maxHouse.id)!!
        val someHouse = dao.find(ORG_ID, someHouse.id)!!
        maxHouseFind.id `should be equal to` maxHouse.id
        maxHouseFind.allowUserId.contains("12345678") `should be equal to` true
        someHouse.id `should be equal to` someHouse.id
        someHouse.allowUserId.isEmpty() `should be equal to` true
    }

    @Test
    fun updateHouse() {
        val houseFind = dao.find(ORG_ID, maxHouse.id)
        houseFind!!.update<House> {
            road = "เชียงราก"
            no = "123/556"
            allowUserId.clear()
            allowUserId.add("999")
        }
        dao.update(ORG_ID, houseFind)
        val houseUpdate = dao.find(ORG_ID, maxHouse.id)

        houseUpdate!!.road `should equal` "เชียงราก"
        houseUpdate.no `should equal` "123/556"
        houseUpdate.allowUserId.contains("999") `should be equal to` true
        houseUpdate.allowUserId.size `should be equal to` 1
    }

    @Test
    fun timestampUpdate() {
        val houseFind = dao.find(ORG_ID, maxHouse.id)
        val oldTimestamp = houseFind!!.timestamp
        Thread.sleep(100)
        houseFind.update<House> {
            road = "เชียงราก"
        }
        dao.update(ORG_ID, houseFind)
        val houseUpdate = dao.find(ORG_ID, maxHouse.id)

        houseFind.timestamp `should not equal` oldTimestamp
        houseFind.timestamp `should equal` houseUpdate!!.timestamp
    }

    @Test
    fun delete() {
        dao.delete(ORG_ID, maxHouse.id)

        dao.find(ORG_ID, maxHouse.id) `should equal` null
    }

    @Test
    fun removeByOrgId() {
        dao.findAll(ORG_ID).size `should be equal to` 2

        dao.removeByOrgId(ORG_ID)

        dao.findAll(ORG_ID).size `should be equal to` 0
    }

    @Test
    fun queryHouse() {
        dao.findAll(ORG_ID, "ลัดดา").size `should be equal to` 2
        dao.findAll(ORG_ID, "999").size `should be equal to` 1
    }

    @Test
    fun queryByLocation() {
        dao.findAll(ORG_ID, haveLocation = true).size `should be equal to` 2
        dao.findAll(ORG_ID, haveLocation = false).size `should be equal to` 0
    }

    @Test
    fun queryWithExplicitVillageName() {
        dao.findAll(ORG_ID, villageName = "ลัดดา").size `should equal` 2
        dao.findAll(ORG_ID, "999", villageName = "ลัดดา").size `should equal` 1
        dao.findAll(ORG_ID, "999", villageName = "ลัด").size `should equal` 1
        dao.findAll(ORG_ID, "999", villageName = "หมู่ 4").size `should equal` 0
    }

    @Test
    fun sorting() {
        dao.insert(ORG_ID, createHouse("11111111112", "900"))
        dao.insert(ORG_ID, createHouse("11111111112", "888/1"))
        dao.insert(ORG_ID, createHouse("11111111112", "888"))
        dao.insert(ORG_ID, createHouse("11111111112", "81"))
        dao.insert(ORG_ID, createHouse("11111111112", "91"))

        val result = dao.findAll(ORG_ID, query = "")

        result.map { it.no } `should equal` listOf(
            "81",
            "91",
            "888",
            "888/1",
            "888/777",
            "900",
            "999/888"
        )
    }
}
