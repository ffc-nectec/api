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

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.printDebug
import ffc.entity.House
import ffc.entity.ffcGson
import ffc.entity.parseTo
import ffc.entity.toJson
import org.bson.Document
import org.bson.types.ObjectId
import java.util.ArrayList
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException


class MongoHouseDao(host: String, port: Int, databaseName: String, collection: String) : HouseDao, MongoAbsConnect(host, port, databaseName, collection) {

    init {
        mongoCreateHouseIndex()
    }

    private fun mongoCreateHouseIndex() {
        try {
            val geoIndex = Document("location", "2dsphere")
            coll2.createIndex(geoIndex, IndexOptions().unique(false))

        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    override fun insert(orgId: String, house: House): House {

        val generateId = ObjectId()

        if (house.coordinates != null) throw BadRequestException("ยกเลิกการใช้งาน house.coordinates แล้วเปลี่ยนไปใช้ house.location แทน")


        val houseInsert: House
        houseInsert = if (house.isTempId) {
            house.copy(generateId.toHexString())
        } else {
            if (house.link != null) {
                house.copy(generateId.toHexString())
            } else {
                throw ForbiddenException("ข้อมูลบ้านที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ link และ isTempId")
            }
        }

        val docHouse = Document.parse(ffcGson.toJson(houseInsert))
        docHouse.append("_id", generateId)
        docHouse.append("orgId", orgId)
        printDebug("Document insert = $docHouse")

        val houseReturn: House

        try {
            coll2.insertOne(docHouse)
            val query = Document("_id", generateId)
            val afterInsertDoc = coll2.find(query).first()
            houseReturn = afterInsertDoc.toJson().parseTo()

        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }

        return houseReturn
    }


    override fun update(house: House) {
        printDebug("Call MongoHouseDao.upldate ${house.toJson()}")
        if (house.coordinates != null) throw BadRequestException("ยกเลิกการใช้งาน house.coordinates แล้วเปลี่ยนไปใช้ house.location แทน")

        val query = Document("_id", ObjectId(house.id))


        printDebug("\tquery old house ")
        val oldHouseDoc = (coll2.find(query).first() ?: throw NotFoundException("ไม่มีบ้านตาม id ให้ Update"))


        val orgId = oldHouseDoc["orgId"].toString()
        printDebug("\tget orgId $orgId")


        //val geoPoint: Document? = createGeoDocument(house)


        printDebug("\tcreate update doc")
        //val updateHouseDoc = createDocument(ObjectId(house.id), orgId, house, geoPoint)

        val updateDoc = Document.parse(ffcGson.toJson(house))
        updateDoc.append("_id", ObjectId(house.id))
        updateDoc.append("orgId", orgId)


        printDebug("\tcall collection.update (oldDoc, updateDoc)")
        printDebug("\t\tOld doc =    $oldHouseDoc")
        printDebug("\t\tUpdate doc = $updateDoc")

        try {
            coll2.replaceOne(query, updateDoc)
        } catch (ex: Exception) {
            ex.printStackTrace()
            val exo = javax.ws.rs.InternalServerErrorException(ex.message)
            exo.stackTrace = ex.stackTrace
            throw exo
        }

        printDebug("\tDone mongo update house.")
    }


    override fun update(houseList: List<House>) {
        houseList.forEach {
            update(it)
        }
    }


    override fun delete(houseId: String) {
        val query = Document("_id", houseId)
        coll2.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบบ้าน id $houseId ให้ลบ")


    }

    override fun findAll(orgId: String, haveLocation: Boolean?): List<House> {
        val query = Document("orgId", orgId)

        when {
            haveLocation == null -> {
            }

            haveLocation -> query.append("location", Document("\$ne", null))
            else -> query.append("location", Document("\$eq", null))
        }

        val listHouse: ArrayList<House> = arrayListOf()

        mongoSafe(object : MongoSafeRun {
            override fun run() {

                val houseListDocument = coll2.find(query)
                printDebug("getHouseInMongo size = ${houseListDocument.count()}")

                houseListDocument.forEach {
                    val house: House = it.toJson().parseTo()
                    listHouse.add(house)
                }

            }

        })

        return listHouse
    }


    override fun find(houseId: String): House {
        printDebug("Call find in house dao.")
        val query = Document("_id", houseId)
        val houseJson = coll2.find(query).first().toJson()

        return houseJson.parseTo()
    }


    override fun removeByOrgId(orgId: String) {
        val query = Document("orgId", orgId)
        coll2.deleteMany(query)

    }

}
