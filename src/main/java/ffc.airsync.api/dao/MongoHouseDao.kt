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

import com.mongodb.BasicDBList
import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.get6DigiId
import ffc.airsync.api.printDebug
import ffc.entity.Address
import ffc.entity.StorageOrg
import ffc.entity.fromJson
import ffc.entity.toJson
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Point
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*
import javax.ws.rs.NotFoundException


class MongoHouseDao(host: String, port: Int, databaseName: String, collection: String) : HouseDao, MongoAbsConnect(host, port, databaseName, collection) {

    init {

        try {


            //Create mongo index.
            val geoIndex = Document("location", "2dsphere")
            coll2.createIndex(geoIndex, IndexOptions().unique(false))


            val houseNumberIndex = Document()
                    .append("hid", 1)
                    .append("orgUuid", 1)
            coll2.createIndex(houseNumberIndex, IndexOptions().unique(true))


            val orgUuidIndex = Document("orgUuid", 1)
            coll2.createIndex(orgUuidIndex, IndexOptions().unique(false))

        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    override fun insert(orgUuid: UUID, house: Address): Address {

        val query = Document("orgUuid", orgUuid.toString())
                .append("hid", house.hid)


        val objId = ObjectId()
        val shortId = objId.get6DigiId()
        house._id = objId.toHexString()
        //house._shortId = shortId

        //{"type":"Point","coordinates":[100.6027899,14.0782897]}

        var geoPoint: Document? = null
        try {
            geoPoint = Document("type", "Point")
                    .append("coordinates", Arrays.asList(house.coordinates!!.longitude, house.coordinates!!.latitude))
        } catch (ex: NullPointerException) {

        }

        val doc = createDocument(objId, orgUuid.toString(), house, geoPoint)


        val houseReturn = house.clone()
        house.coordinates = null
        doc.append("property", house.toJson())
        printDebug("Document insert = $doc")

        //coll2.deleteOne(query)
        try {
            coll2.insertOne(doc)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }

        return houseReturn
    }

    private fun createDocument(objId: ObjectId, orgUuid: String, house: Address, geoPoint: Document?): Document {
        val doc = Document("_id", objId)
                //.append("_shortId", shortId)
                .append("orgUuid", orgUuid)
                .append("hid", house.hid)
                .append("latitude", house.coordinates?.latitude)
                .append("longitude", house.coordinates?.longitude)
                .append("location", geoPoint)
        return doc
    }


    override fun update(house: Address) {
        printDebug("Call MongoHouseDao.upldate ${house.toJson()}")
        val query = Document("_id", ObjectId(house._id))


        printDebug("\tquery old house ")
        val oldDoc = (coll2.find(query).first() ?: throw NotFoundException("ไม่พบ Object ให้ Update"))


        val orgUuid = oldDoc["orgUuid"].toString()
        printDebug("\tget orgUuid $orgUuid")


        var geoPoint: Document? = null
        try {
            geoPoint = Document("type", "Point")
                    .append("coordinates", Arrays.asList(house.coordinates!!.longitude, house.coordinates!!.latitude))
        } catch (ex: NullPointerException) {

        }


        printDebug("\tcreate update doc")
        val updateDoc = createDocument(ObjectId(house._id), orgUuid, house, geoPoint)

        printDebug("\t1")
        house.coordinates = null
        house.pcuCode = oldDoc["property"].toString().fromJson<Address>().pcuCode
        printDebug("\t2")
        updateDoc.append("property", house.toJson())


        printDebug("\tcall collection.update (oldDoc, updateDoc)")
        printDebug("\t\tOld doc =    $oldDoc")
        printDebug("\t\tUpdate doc = $updateDoc")

        try {
            coll2.replaceOne(query, updateDoc)
        } catch (ex: Exception) {
            ex.printStackTrace()
            val exo = javax.ws.rs.InternalServerErrorException(ex.message)
            exo.stackTrace = ex.stackTrace
            throw exo
        }

        printDebug("\t3")
    }


    override fun update(houseList: List<Address>) {
        houseList.forEach {
            update(it)
        }
    }


    override fun find(latlng: Boolean): List<StorageOrg<Address>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun find(orgUuid: UUID, haveLocation: Boolean?): List<StorageOrg<Address>> {
        var query = Document("orgUuid", orgUuid.toString())



        if (haveLocation == null) {
        } else if (haveLocation) {

            val or1 = BasicDBList()
            or1.add(Document("longitude", Document("\$ne", null)))
            or1.add(Document("longitude", Document("\$ne", 0.0)))
            or1.add(Document("latitude", Document("\$ne", null)))
            or1.add(Document("latitude", Document("\$ne", 0.0)))
            query = query.append("\$and", or1)


        } else {

            val or1 = BasicDBList()
            or1.add(Document("longitude", Document("\$eq", null)))
            or1.add(Document("longitude", Document("\$eq", 0.0)))
            or1.add(Document("latitude", Document("\$eq", null)))
            or1.add(Document("latitude", Document("\$eq", 0.0)))
            query = query.append("\$or", or1)

        }

        val listHouse: ArrayList<StorageOrg<Address>> = arrayListOf()

        mongoSafe(object : MongoSafeRun {
            override fun run() {

                val cursor = coll2.find(query)
                printDebug("getHouseInMongo size = ${cursor.count()}")

                cursor.forEach {
                    val property = it.get("property")
                    //printDebug(property)

                    val house: Address = property.toString().fromJson()
                    try {
                        house.coordinates = LatLng(it["latitude"].toString().toDouble(), it["longitude"].toString().toDouble())
                        house.location = Point(LatLng(it["latitude"].toString().toDouble(), it["longitude"].toString().toDouble()))
                    } catch (ex: java.lang.NullPointerException) {
                        house.coordinates = null
                        house.location = null
                    } catch (ex: java.lang.NumberFormatException) {
                        house.coordinates = null
                        house.location = null
                    }
                    printDebug(house)


                    listHouse.add(StorageOrg(orgUuid, house))
                }

            }

        })


        return listHouse
    }


    override fun findByHouseId(orgUuid: UUID, hid: Int): StorageOrg<Address>? {
        printDebug("House mongo dao findByHouseId\n\torgUuid $orgUuid hid $hid")
        val query = Document("orgUuid", orgUuid.toString())
                .append("hid", hid)


        val dbObj = coll2.find(query).first()
        printDebug("\tQuery found=$dbObj")
        if (dbObj == null) return null


        printDebug("\t\tproperty = ${dbObj["property"]}")


        val house: Address = dbObj["property"].toString().fromJson()
        printDebug("\tset lat long")
        house.coordinates = LatLng(dbObj.get("latitude").toString().toDouble(), dbObj["longitude"].toString().toDouble())


        printDebug("\tReturn")
        return StorageOrg(orgUuid, house)
    }


    override fun findByHouse_Id(orgUuid: UUID, _id: String): StorageOrg<Address>? {
        printDebug("House mongo dao findByHouse_Id\n\torgUuid $orgUuid _id $_id")
        val query = Document("orgUuid", orgUuid.toString())
                .append("_id", ObjectId(_id))

        printDebug("\tcreate query object finish $query")

        val dbObj = coll2.find(query).first() ?: throw NotFoundException("findByHouse_Id  ไม่พบ")
        printDebug("\tQuery property = ${dbObj.get("property")}")


        val house: Address = dbObj.get("property").toString().fromJson()
        printDebug("\tset lat long")
        house.coordinates = LatLng(dbObj.get("latitude").toString().toDouble(), dbObj.get("longitude").toString().toDouble())


        printDebug("\tReturn")
        return StorageOrg(orgUuid, house)
    }


    override fun removeByOrgUuid(orgUuid: UUID) {
        val query = Document("orgUuid", orgUuid.toString())
        coll2.deleteMany(query)

    }

}
