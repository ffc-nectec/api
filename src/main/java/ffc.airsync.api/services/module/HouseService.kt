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
import ffc.airsync.api.dao.DaoFactory
import ffc.airsync.api.printDebug
import ffc.entity.Address
import ffc.entity.People
import ffc.entity.toJson
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import me.piruin.geok.geometry.Geometry
import me.piruin.geok.geometry.Point
import org.joda.time.DateTime
import java.util.UUID
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException

//val org = orgDao.findById(orgId)
object HouseService {

    val houseDao = DaoFactory().buildHouseDao()

    fun createByOrg(orgId: String, houseList: List<Address>): List<Address> {

        val houseReturn = arrayListOf<Address>()
        houseList.forEach {
            val houseUpdate = createByOrg(orgId, it)
            houseReturn.add(houseUpdate)
        }

        return houseReturn
    }

    fun createByOrg(orgId: String, house: Address): Address {
        val org = orgDao.findById(orgId)
        house._sync = true
        try {
            house.location = Point(house.coordinates!!.latitude, house.coordinates!!.longitude)
        } catch (ex: NullPointerException) {

        }

        if (house.hid!! < 0) throw BadRequestException("")
        return houseDao.insert(org.uuid, house)
    }

    fun createByUser(orgId: String, houseList: List<Address>): List<Address> {
        val houseReturn = arrayListOf<Address>()
        houseList.forEach {
            val houseUpdate = createByUser(orgId, it)
            houseReturn.add(houseUpdate)
        }

        return houseReturn
    }

    fun createByUser(orgId: String, house: Address): Address {
        val org = orgDao.findById(orgId)
        house._sync = false
        return houseDao.insert(org.uuid, house)
    }


    fun update(role: TokenMessage.TYPEROLE, orgId: String, house: Address, house_id: String) {

        printDebug("Update house role $role orgid $orgId house_id $house_id house ${house.toJson()}")
        if (house._id == "") throw BadRequestException("ไม่มี _id")


        house.people = null
        house.haveChronics = null





        if (house_id == house._id) {
            val firebaseTokenGropOrg = arrayListOf<String>()
            var listMessage: List<StorageOrg<TokenMessage>>? = null
            val org = orgDao.findById(orgId)
            val orgUuid = org.uuid


            if (role == TokenMessage.TYPEROLE.USER) {
                listMessage = token.findByOrgId(orgUuid)
                house._sync = false
                house.dateUpdate = DateTime.now()
                printDebug("\t\tFound mobile token")
            } else if (role == TokenMessage.TYPEROLE.ORG) {

                printDebug("\tFind org token")
                house._sync = true
                listMessage = token.findByOrgId(orgUuid)
                printDebug("\t\tFound org token")
            }

            printDebug("\tGroup firebase token")
            listMessage?.forEach {
                firebaseTokenGropOrg.add(it.data.firebaseToken ?: "")
                printDebug("\tmobile $it")
            }


            firebaseTokenGropOrg.add(org?.firebaseToken ?: "")
            printDebug("\torg ${org?.firebaseToken}")


            houseDao.update(house.clone())


            printDebug("Call send notification size list token = ${firebaseTokenGropOrg.size} ")
            firebaseTokenGropOrg.forEach {
                printDebug("\ttoken=$it")
                if (it.isNotEmpty())
                    Message.builder().putHouseData(house, it, orgId)
            }


        } else {  //ทำงานเมื่อ _id ใน url ไม่ตรงกับ _id ที่อยู่ในข้อมูลที่ส่งเข้ามา update
            printDebug("House id not eq update houseIdParameter=$house_id houseIdInData=${house._id}")
            throw BadRequestException("House _id not eq update")
        }
        Thread.sleep(200)
    }


    fun getGeoJsonHouse(orgId: String, page: Int = 1, per_page: Int = 200, hid: Int = -1, haveLocation: Boolean?, urlString: String): FeatureCollection<Address> {


        printDebug("haveLocation = $haveLocation Url query = $urlString")
        if (haveLocation == false) {
            if (urlString.trimEnd().endsWith("haveLocation=") || urlString.trimEnd().endsWith("haveLocation")) {
                throw javax.ws.rs.InternalServerErrorException("Parameter query $urlString")
            }

        }

        val org = orgDao.findById(orgId)
        val orgUuid = org.id



        printDebug("Search house match")
        val listHouse: List<Address>


        if (hid > 0) {
            val house = houseDao.findByHouseId(orgUuid, hid)
                    ?: throw NotFoundException("ไม่พบ hid บ้าน")
            listHouse = ArrayList()
            listHouse.add(house)
        } else {
            listHouse = houseDao.findAll(orgUuid, haveLocation)
        }
        printDebug("count house = ${listHouse.count()}")


        val geoJson = FeatureCollection<Address>()
        val count = listHouse.count()

        itemRenderPerPage(page, per_page, count, object : AddItmeAction {
            override fun onAddItemAction(itemIndex: Int) {
                try {
                    //printDebug("Loop count $it")
                    val data = listHouse[itemIndex]
                    val feture = createGeo(data.data, orgUuid)
                    geoJson.features.add(feture)
                    //printDebug("Add feture success")
                } catch (ex: kotlin.KotlinNullPointerException) {
                    ex.printStackTrace()
                }
            }
        })

        return geoJson
    }

    fun getJsonHouse(orgId: String, page: Int = 1, per_page: Int = 200, hid: Int = -1, haveLocation: Boolean?, urlString: String): List<Address> {

        val geoJsonHouse = getGeoJsonHouse(orgId, page, per_page, hid, haveLocation, urlString)


        val houseList = arrayListOf<Address>()


        geoJsonHouse.features.forEach {
            val house = it.properties
            if (house != null)
                houseList.add(house)
        }

        return houseList

    }

    fun getSingle(orgId: String, houseId: String): Address {

        val singleHouseGeo = getSingleGeo(orgId, houseId)
        val house = singleHouseGeo.features.get(0).properties

        return house ?: throw NotFoundException("ไม่มีรายการบ้าน ที่ระบุ")
    }

    fun getSingleGeo(orgId: String, houseId: String): FeatureCollection<Address> {

        val org = orgDao.findById(orgId)
        val orgUuid = org.uuid

        val geoJson = FeatureCollection<Address>()
        val house: StorageOrg<Address>


        printDebug("\thouse findBy_ID OrgUuid = ${orgUuid} houseId = $houseId")
        house = houseDao.findByHouse_Id(orgUuid, houseId) ?: throw NotFoundException("ไม่พบข้อมูลบ้านที่ระบุ")


        printDebug("\t\t$house")
        val feture = createGeo(house.data, orgUuid)
        geoJson.features.add(feture)


        return geoJson
    }


    private fun createGeo(data: Address, orgUuid: UUID): Feature<Address> {
        var point: Geometry
        val houseId = data.hid ?: -1
        val house = data

        if (data.coordinates != null) {
            point = Point(data.coordinates!!)
        } else {
            point = Point(LatLng(0.0, 0.0))
        }

        house.people = personDao.getPeopleInHouse(orgUuid, houseId)

        house.haveChronics = houseIsChronic(house.people)


        printDebug("Create feture")
        val feture: Feature<Address> = Feature(
                geometry = point,
                properties = house)


        return feture
    }

    private fun houseIsChronic(peopleList: List<People>?): Boolean {
        if (peopleList == null) return false
        val personChronic = peopleList.find {
            it.chronics != null
        }
        return personChronic != null


    }


}


