/*
 * Copyright (c) 2018 NECTEC
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
 */

package ffc.airsync.api.services.house

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.notification.broadcastMessage
import ffc.airsync.api.services.notification.notification
import ffc.airsync.api.services.person.persons
import ffc.entity.Person
import ffc.entity.User
import ffc.entity.User.Role.ADMIN
import ffc.entity.User.Role.ORG
import ffc.entity.copy
import ffc.entity.gson.toJson
import ffc.entity.place.House
import ffc.entity.update
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.ws.rs.BadRequestException

object HouseService {
    fun createByOrg(orgId: String, houseList: List<House>): List<House> {
        printDebug("create house by org.")
        return houses.insert(orgId, houseList.map {
            if (it.link == null) throw BadRequestException("เมื่อสร้างด้วย org จำเป็นต้องมีข้อมูล link")
            it.link!!.isSynced = true
            it
        })
    }

    fun createByOrg(orgId: String, house: House): House {
        if (house.link == null) throw BadRequestException("เมื่อสร้างด้วย org จำเป็นต้องมีข้อมูล link")
        house.link!!.isSynced = true
        return houses.insert(orgId, house)
    }

    fun createByUser(orgId: String, houseList: List<House>): List<House> {
        return houses.insert(orgId, houseList.map {
            if (it.link != null) throw BadRequestException("เมื่อสร้างด้วย user ไม่ต้องมีข้อมูล link")
            it
        })
    }

    fun createByUser(orgId: String, house: House): House {
        if (house.link != null) throw BadRequestException("เมื่อสร้างด้วย user ไม่ต้องมีข้อมูล link")
        return houses.insert(orgId, house)
    }

    fun update(role: User.Role, orgId: String, house: House, houseId: String): House {
        printDebug("Update house role $role orgid $orgId house_id $houseId house ${house.toJson()}")

        require(houseId == house.id) { "เลขบ้านที่ระบุใน url part ไม่ตรงกับข้อมูล id ที่ต้องการแก้ไข" }
        require(house.id != "") { "ไม่มี id ไม่มีการใช้ตัวแปร _id แล้ว" }

        house.people.clear()

        printDebug("\t\tGet firebase token.")
        val firebaseTokenGropOrg = notification.getFirebaseToken(orgId)

        printDebug("\tUpdate house to dao.")

        when (role) {
            ORG -> house.update(house.timestamp) {
                house.link?.isSynced = true
            }
            ADMIN -> house.update(house.timestamp) {
                house.link?.isSynced = true
            }
            else -> house.update {
                house.link?.isSynced = false
            }
        }

        val houseUpdate = houses.update(orgId, house.copy())

        printDebug("Call send notification size list token = ${firebaseTokenGropOrg.size} ")
        notification.broadcastMessage(orgId, house)

        return houseUpdate!!
    }

    fun getHouses(orgId: String, query: String? = null, haveLocation: Boolean? = null): List<House> {
        return houses.findAll(orgId, query, haveLocation)
    }

    fun getSingleGeo(orgId: String, houseId: String): FeatureCollection<House>? {
        val house = getSingle(orgId, houseId) ?: return null
        if (house.location == null) return null
        val geoJson = FeatureCollection<House>()
        geoJson.features.add(house.toGeoJsonFeature())
        return geoJson
    }

    fun getSingle(orgId: String, houseId: String): House? {
        return houses.find(orgId, houseId)
    }

    fun getPerson(orgId: String, houseId: String): List<Person> {
        val person = persons.getPeopleInHouse(orgId, houseId)
        return if (person.isNotEmpty()) person else throw NullPointerException("ไม่พบคนในบ้าน $houseId")
    }
}

private fun House.toGeoJsonFeature(): Feature<House> {
    return Feature(this.location!!, this)
}
