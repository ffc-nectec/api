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

package ffc.airsync.api.services.module

import com.google.firebase.messaging.Message
import ffc.airsync.api.printDebug
import ffc.entity.House
import ffc.entity.User
import ffc.entity.copy
import ffc.entity.gson.toJson
import ffc.entity.update
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.ws.rs.BadRequestException

object HouseService {

    fun createByOrg(orgId: String, houseList: List<House>): List<House> {
        printDebug("create house by org.")
        val houseReturn = arrayListOf<House>()
        try {
            houseList.forEach {
                println("\tHouse original ${it.toJson()}")
                val houseUpdate = createByOrg(orgId, it)
                println("\tHouse update ${houseUpdate.toJson()}")
                houseReturn.add(houseUpdate)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        return houseReturn
    }

    fun createByOrg(orgId: String, house: House): House {
        if (house.link == null) throw BadRequestException("เมื่อสร้างด้วย org จำเป็นต้องมีข้อมูล link")
        house.link!!.isSynced = true
        return houseDao.insert(orgId, house)
    }

    fun createByUser(orgId: String, houseList: List<House>): List<House> {
        val houseReturn = arrayListOf<House>()
        houseList.forEach {
            val houseUpdate = createByUser(orgId, it)
            houseReturn.add(houseUpdate)
        }
        return houseReturn
    }

    fun createByUser(orgId: String, house: House): House {
        if (house.link != null) throw BadRequestException("เมื่อสร้างด้วย user ไม่ต้องมีข้อมูล link")
        return houseDao.insert(orgId, house)
    }

    fun update(role: User.Role, orgId: String, house: House, houseId: String): House {
        printDebug("Update house role $role orgid $orgId house_id $houseId house ${house.toJson()}")

        if (houseId != house.id) throw BadRequestException("เลขบ้านที่ระบุใน url part ไม่ตรงกับข้อมูล id ที่ต้องการแก้ไข")
        if (house.id == "") throw BadRequestException("ไม่มี id ไม่มีการใช้ตัวแปร _id แล้ว")

        house.people = null

        printDebug("\t\tGet firebase token.")
        val firebaseTokenGropOrg = orgDao.getFirebaseToken(orgId)

        printDebug("\tUpdate house to dao.")

        if (role == User.Role.ORG) {
            house.update<House>(house.timestamp) {
                house.link?.isSynced = true
            }
        } else if (role == User.Role.USER) {
            house.update<House> {
                house.link?.isSynced = false
            }
        }

        val houseUpdate = houseDao.update(house.copy<House>())

        printDebug("Call send notification size list token = ${firebaseTokenGropOrg.size} ")
        try {
            firebaseTokenGropOrg.forEach {
                printDebug("\ttoken=$it")
                if (it.isNotEmpty()) Message.builder().putHouseData(house, it, orgId)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return houseUpdate!!
    }

    private fun queryHouse(orgId: String, haveLocationFilter: Boolean?): List<House> {
        return houseDao.findAll(orgId, haveLocationFilter).toMutableList().apply {
            when (haveLocationFilter) {
                true -> removeIf { it.location == null }
                false -> removeIf { it.location != null }
            }
        }
    }

    fun getHouses(orgId: String, page: Int = 1, per_page: Int = 200, haveLocation: Boolean?): List<House> {
        return queryHouse(orgId, haveLocation).paging(page, per_page)
    }

    fun getSingle(houseId: String): House? {
        return houseDao.find(houseId) ?: return null
    }

    fun getSingleGeo(orgId: String, houseId: String): FeatureCollection<House>? {
        val house = getSingle(houseId) ?: return null
        if (house.location == null) return null

        val geoJson = FeatureCollection<House>()
        geoJson.features.add(house.toGeoJsonFeature())
        return geoJson
    }
}

private fun House.toGeoJsonFeature(): Feature<House> {
    return Feature(this.location!!, this)
}