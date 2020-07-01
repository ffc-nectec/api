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

import ffc.entity.Person
import ffc.entity.ThaiCitizenId
import ffc.entity.ThaiHouseholdId
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import ffc.entity.place.House
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should be equal to`
import org.joda.time.LocalDate
import org.junit.Test

class SurveyorProcessTest {
    private val prc = SurveyorProcess()

    val houseHaveLocation = createHouse("12332569057", "1", Point(13.111, 110.111))
    val houseNullLocation = createHouse("38276364212", "2", null)
    val houseZeroLocation = createHouse("87909874332", "3", Point(0.0, 0.0))

    @Test
    fun `อัพเดทบ้านมีพิกัดใส่บ้านที่ไม่มีพิกัด`() {
        val result = prc.process(houseZeroLocation, houseHaveLocation)

        result.id `should be equal to` houseZeroLocation.id
        result.no!! `should be equal to` houseZeroLocation.no!!
        result.location!!.coordinates.latitude `should be equal to` houseHaveLocation.location!!.coordinates.latitude
        result.location!!.coordinates.longitude `should be equal to` houseHaveLocation.location!!.coordinates.longitude
    }

    @Test
    fun `อัพเดทบ้านมีพิกัดใส่บ้าน null`() {
        val result = prc.process(houseNullLocation, houseHaveLocation)

        result.id `should be equal to` houseNullLocation.id
        result.no!! `should be equal to` houseNullLocation.no!!
        result.location!!.coordinates.latitude `should be equal to` houseHaveLocation.location!!.coordinates.latitude
        result.location!!.coordinates.longitude `should be equal to` houseHaveLocation.location!!.coordinates.longitude
    }

    @Test(expected = Exception::class)
    fun `อัพเดทบ้านมีพิกัดใส่บ้านมีพิกัด`() {
        val test = createHouse("97779898767", "1", Point(13.777, 110.777))
        prc.process(test, houseHaveLocation)
    }

    fun createHouse(identity: String, no: String, point: Point?): House {
        return House().apply {
            this.identity = ThaiHouseholdId(identity)
            this.no = no
            road = "สาธร"
            villageName = "ลัดดา"
            location = point
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
}
