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

package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.Address
import ffc.entity.StorageOrg
import java.util.*
import javax.ws.rs.NotFoundException

class InMemoryHouseDao : HouseDao {

    private constructor()

    companion object {
        val instant = InMemoryHouseDao()
    }


    val houseList = arrayListOf<StorageOrg<Address>>()


    override fun insert(orgUuid: UUID, house: Address): Address {
        //houseList.removeIf { it.uuid == orgUuid && it.data.identity?.id == house.identity?.id }
        //have bug _id
        printDebug("Insert house = ${house.identity?.id} XY= ${house.coordinates}")
        houseList.add(StorageOrg(orgUuid, house))
        return house
    }


    override fun findByHouse_Id(orgUuid: UUID, _id: String): StorageOrg<Address>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(house: Address) {
        printDebug("Update house = ${house.identity?.id} XY= ${house.coordinates}")
        val houseUpdate = houseList.find {
            it.data._id == house._id && it.data.hid == house.hid
        } ?: throw NotFoundException("ไม่มีรายการบ้านให้ Update")
        houseUpdate.data = house

    }

    override fun update(houseList: List<Address>) {
        houseList.forEach {
            update(it)
        }

    }

    override fun find(latlng: Boolean): List<StorageOrg<Address>> {
        if (latlng)
            return houseList.filter { it.data.coordinates!!.latitude != 0.0 || it.data.coordinates!!.longitude != 0.0 }
        else
            return houseList
    }

    override fun findByHouseId(orgUuid: UUID, houseId: Int): StorageOrg<Address>? {

        return houseList.find { it.data.hid == houseId && it.uuid == orgUuid }
    }

    override fun find(orgUuid: UUID, haveLocation: Boolean?): List<StorageOrg<Address>> {
        if (haveLocation == null) {
            return houseList.filter {
                it.uuid == orgUuid
            }
        } else if (haveLocation) {
            return houseList.filter {
                (it.data.coordinates!!.latitude != 0.0 || it.data.coordinates!!.longitude != 0.0) && it.uuid == orgUuid
            }
        } else
            return houseList.filter {
                (it.data.coordinates!!.latitude == 0.0 || it.data.coordinates!!.longitude == 0.0) && it.uuid == orgUuid
            }


    }


    override fun removeByOrgUuid(orgUuid: UUID) {
        houseList.removeIf { it.uuid == orgUuid }
    }
}
