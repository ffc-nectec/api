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

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.printDebug
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.entity.House
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import java.util.ArrayList
import javax.ws.rs.NotFoundException

internal class MongoHouseDao(host: String, port: Int) : HouseDao, MongoAbsConnect(host, port, "ffc", "house") {

    init {
        mongoCreateHouseIndex()
    }

    private fun mongoCreateHouseIndex() {
        try {
            dbCollection.createIndex("location" equal "2dsphere", IndexOptions().unique(false))
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    override fun insert(orgId: String, house: House): House {
        val docHouse = house.buildInsertBson()
        docHouse.append("orgId", orgId)

        return dbCollection.ffcInsert(docHouse)
    }

    override fun update(house: House): House? {
        printDebug("Call MongoHouseDao.upldate ${house.toJson()}")
        val query = "id" equal house.id

        printDebug("\tquery old house ")
        val oldHouseDoc = (dbCollection.find(query).first() ?: throw NullPointerException("ไม่มีบ้านตาม id ให้ Update"))
        val orgId = oldHouseDoc["orgId"].toString()
        printDebug("\tget orgId $orgId")
        printDebug("\tcreate update doc")
        // val updateHouseDoc = createDocument(ObjectId(house.id), orgId, house, geoPoint)
        val updateDoc = Document.parse(house.toJson())
        updateDoc.append("id", house.id)
        updateDoc.append("orgId", orgId)

        printDebug("\tcall collection.update (oldDoc, updateDoc)")
        printDebug("\t\tOld doc =    $oldHouseDoc")
        printDebug("\t\tUpdate doc = $updateDoc")

        try {
            dbCollection.replaceOne(query, updateDoc)
        } catch (ex: Exception) {
            ex.printStackTrace()
            val exo = javax.ws.rs.InternalServerErrorException(ex.message)
            exo.stackTrace = ex.stackTrace
            throw exo
        }
        printDebug("\tDone mongo update house.")
        return find(house.id)
    }

    override fun update(houseList: List<House>): List<House> {
        val houseUpdate = arrayListOf<House>()
        houseList.forEach {
            val house = update(it)
            if (house != null)
                houseUpdate.add(house)
        }
        return houseUpdate
    }

    override fun delete(houseId: String) {
        val query = "id" equal houseId
        dbCollection.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบบ้าน id $houseId ให้ลบ")
    }

    override fun findAll(orgId: String, haveLocation: Boolean?): List<House> {
        val query = "orgId" equal orgId
        when {
            haveLocation == null -> {
            }
            haveLocation -> query.append("location", "\$ne" equal null)
            else -> query.append("location", "\$eq" equal null)
        }
        val listHouse: ArrayList<House> = arrayListOf()

        mongoSafe(object : MongoSafeRun {
            override fun run() {
                val houseListDocument = dbCollection.find(query)
                printDebug("getHouseInMongo size = ${houseListDocument.count()}")

                houseListDocument.forEach {
                    val house: House = it.toJson().parseTo()
                    listHouse.add(house)
                }
            }
        })
        return listHouse
    }

    override fun find(houseId: String): House? {
        printDebug("Call find in house dao.")
        val houseJson = dbCollection.find("id" equal houseId)?.first()?.toJson()
        return houseJson?.parseTo()
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgId" equal orgId)
    }
}