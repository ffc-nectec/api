/*
 * Copyright (c) 2561 NECTEC
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
import ffc.entity.gson.toJson
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import me.piruin.geok.geometry.Geometry
import me.piruin.geok.geometry.Point
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException

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
            house.update<House> {
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
        // Thread.sleep(200)
        return houseUpdate!!
    }

    fun getGeoJsonHouse(orgId: String, page: Int = 1, per_page: Int = 200, haveLocation: Boolean?, urlString: String): FeatureCollection<House> {
        printDebug("haveLocation = $haveLocation Url query = $urlString")
        if (haveLocation == false) {
            if (urlString.trimEnd().endsWith("haveLocation=") || urlString.trimEnd().endsWith("haveLocation")) {
                throw javax.ws.rs.InternalServerErrorException("Parameter query $urlString")
            }
        }

        printDebug("Search house match")
        val listHouse = arrayListOf<House>().apply {
            addAll(houseDao.findAll(orgId, haveLocation))
            removeIf {
                it.location?.coordinates?.longitude == 0.0
            }
        }

        printDebug("count house = ${listHouse.count()}")

        val geoJson = FeatureCollection<House>()
        val count = listHouse.count()

        itemRenderPerPage(page, per_page, count, object : AddItmeAction {
            override fun onAddItemAction(itemIndex: Int) {
                try {
                    // printDebug("Loop count $it")
                    val house = listHouse[itemIndex]

                    val feture: Feature<House> = createGeoFeature(house)

                    geoJson.features.add(feture)
                    // printDebug("Add feture success")
                } catch (ex: kotlin.KotlinNullPointerException) {
                    ex.printStackTrace()
                }
            }
        })
        return geoJson
    }

    fun getJsonHouse(orgId: String, page: Int = 1, per_page: Int = 200, haveLocation: Boolean?, urlString: String): List<House> {
        val geoJsonHouse = getGeoJsonHouse(orgId, page, per_page, haveLocation, urlString)
        val houseList = arrayListOf<House>()

        geoJsonHouse.features.forEach {
            val house = it.properties
            if (house != null) houseList.add(house)
        }
        return houseList
    }

    fun getSingle(orgId: String, houseId: String): House {
        val singleHouseGeo = getSingleGeo(orgId, houseId)
        val house = singleHouseGeo.features[0].properties
        return house ?: throw NotFoundException("ไม่มีรายการบ้าน ที่ระบุ")
    }

    fun getSingleGeo(orgId: String, houseId: String): FeatureCollection<House> {
        printDebug("\thouse findBy_ID OrgUuid = $orgId houseId = $houseId")

        val geoJson = FeatureCollection<House>()
        val house: House = houseDao.find(houseId) ?: throw NotFoundException("ไม่พบรหัสบ้าน $houseId")

        printDebug("\t\t$house")
        val feature = createGeoFeature(house)
        geoJson.features.add(feature)

        return geoJson
    }

    private fun createGeoFeature(house: House): Feature<House> {
        var point: Geometry = Point(0.0, 0.0)

        try {
            val coordinates = house.location?.coordinates
            point = Point(coordinates!!.latitude, coordinates.longitude)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
        return Feature(geometry = point, properties = house)
    }
}
