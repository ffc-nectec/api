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

import ffc.airsync.api.getLogger
import ffc.airsync.api.services.notification.broadcastMessage
import ffc.airsync.api.services.notification.notification
import ffc.airsync.api.services.person.persons
import ffc.airsync.api.services.util.containsSome
import ffc.entity.Person
import ffc.entity.User
import ffc.entity.copy
import ffc.entity.place.House
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.ws.rs.ForbiddenException

class HouseService(val housesDao: HouseDao = houses) {
    val logger = getLogger()

    fun create(
        orgId: String,
        role: List<User.Role>,
        houseList: List<House>,
        block: Int = -1
    ): List<House> {
        return when {
            role.containsSome(User.Role.ADMIN) -> createByOrg(orgId, houseList, block)
            role.containsSome(User.Role.SURVEYOR) -> createByUser(orgId, houseList, block)
            else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
        }
    }

    private fun createByOrg(orgId: String, houseList: List<House>, block: Int = -1): List<House> {
        val house = houseList.map {
            require(it.link != null) { "เมื่อสร้างด้วย org จำเป็นต้องมีข้อมูล link" }
            it.link!!.isSynced = true
            it
        }

        return if (block < 0)
            housesDao.insert(orgId, house)
        else
            housesDao.insertBlock(orgId, block, house)
    }

    private fun createByUser(orgId: String, houseList: List<House>, block: Int = -1): List<House> {
        houseList.forEach {
            require(it.link == null) { "เมื่อสร้างด้วย user ไม่ต้องมีข้อมูล link" }
        }

        return if (block < 0)
            housesDao.insert(orgId, houseList)
        else
            housesDao.insertBlock(orgId, block, houseList)
    }

    fun create(
        orgId: String,
        role: List<User.Role>,
        house: House
    ): House {
        return when {
            role.containsSome(User.Role.ADMIN) ->
                houseService.createByOrg(orgId, house)
            role.containsSome(User.Role.SURVEYOR) ->
                houseService.createByUser(orgId, house)
            else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
        }
    }

    private fun createByOrg(orgId: String, house: House): House {
        require(house.link != null) { "เมื่อสร้างด้วย org จำเป็นต้องมีข้อมูล link" }
        house.link!!.isSynced = true
        return housesDao.insert(orgId, house)
    }

    private fun createByUser(orgId: String, house: House): House {
        require(house.link == null) { "เมื่อสร้างด้วย user ไม่ต้องมีข้อมูล link" }
        return housesDao.insert(orgId, house)
    }

    fun update(orgId: String, house: House, houseId: String): House {
        require(houseId == house.id) { "เลขบ้านที่ระบุใน url part ไม่ตรงกับข้อมูล id ที่ต้องการแก้ไข" }
        require(house.id != "") { "ไม่มี id ไม่มีการใช้ตัวแปร _id แล้ว" }

        house.people.clear()

        logger.debug("\t\tGet firebase token.")
        val firebaseTokenGropOrg = notification.getFirebaseToken(orgId)

        logger.debug("\tUpdate house to dao.")

        val houseUpdate = housesDao.update(orgId, house.copy())

        logger.debug("send notification size list token = ${firebaseTokenGropOrg.size} ")
        notification.broadcastMessage(orgId, house)

        return houseUpdate!!
    }

    fun getHouses(orgId: String, query: String? = null, haveLocation: Boolean? = null): List<House> {
        var houseNo: String? = null
        var villageName: String? = null
        query?.let {
            val result = Regex("(^.+) +(.+)").find(query)
            houseNo = result?.groupValues?.get(1)
            villageName = result?.groupValues?.get(2)
        }
        return housesDao.findAll(orgId, houseNo ?: query, haveLocation, villageName)
    }

    fun getSingleGeo(orgId: String, houseId: String): FeatureCollection<House>? {
        val house = getSingle(orgId, houseId) ?: return null
        if (house.location == null) return null
        return FeatureCollection(house.toGeoJsonFeature())
    }

    fun getSingle(orgId: String, houseId: String): House? {
        return housesDao.find(orgId, houseId)
    }

    fun getPerson(orgId: String, houseId: String): List<Person> {
        val person = persons.getPeopleInHouse(orgId, houseId)
        return if (person.isNotEmpty()) person else throw NoSuchElementException("ไม่พบคนในบ้าน $houseId")
    }
}

private fun House.toGeoJsonFeature(): Feature<House> {
    return Feature(this.location!!, this)
}

internal val houseService by lazy { HouseService() }
