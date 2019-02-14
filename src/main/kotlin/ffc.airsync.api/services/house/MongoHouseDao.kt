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
import com.mongodb.client.model.Sorts
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.MongoSyncDao
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.callErrorIgnore
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.listOf
import ffc.airsync.api.services.util.plus
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.place.House
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import javax.ws.rs.NotFoundException

internal class MongoHouseDao : HouseDao, MongoSyncDao<House>("ffc", "house") {

    init {
        createIndexByOrgIndex()
        mongoCreateHouseIndex()
    }

    private fun mongoCreateHouseIndex() {
        callErrorIgnore {
            dbCollection.createIndex("location" equal "2dsphere", IndexOptions().unique(false))
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
        }
    }

    override fun insert(orgId: String, house: House): House {
        val docHouse = house.buildInsertBson()
        docHouse.append("orgIndex", ObjectId(orgId))

        return dbCollection.ffcInsert(docHouse)
    }

    override fun insert(orgId: String, house: List<House>): List<House> {
        val doc = house.map {
            val docHouse = it.buildInsertBson()
            docHouse.append("orgIndex", ObjectId(orgId))
            docHouse
        }

        return dbCollection.ffcInsert(doc)
    }

    override fun update(orgId: String, house: House): House? {
        val query = "_id" equal ObjectId(house.id)

        logger.trace("\tquery old house ")
        val oldHouseDoc = (dbCollection.find(query).first() ?: throw NullPointerException("ไม่มีบ้านตาม id ให้ Update"))

        check(oldHouseDoc["orgIndex"] == ObjectId(orgId)) { "houseId out of organization" }

        logger.trace("\tget orgId $orgId")
        logger.trace("\tcreate update doc")
        // val updateHouseDoc = createDocument(ObjectId(house.id), orgId, house, geoPoint)
        val updateDoc = Document.parse(house.toJson())
        updateDoc.append("id", house.id)
        updateDoc.append("orgIndex", ObjectId(orgId))

        logger.trace("\tcall collection.update (oldDoc, updateDoc)")
        logger.trace("\t\tOld doc =    $oldHouseDoc")
        logger.trace("\t\tUpdate doc = $updateDoc")

        try {
            dbCollection.replaceOne(query, updateDoc)
        } catch (ex: Exception) {
            val exo = javax.ws.rs.InternalServerErrorException(ex.message)
            exo.stackTrace = ex.stackTrace
            throw exo
        }
        logger.debug("\tDone mongo update house.")
        return find(orgId, house.id)
    }

    override fun update(orgId: String, houseList: List<House>): List<House> {
        val houseUpdate = arrayListOf<House>()
        houseList.forEach {
            val house = update(orgId, it)
            if (house != null)
                houseUpdate.add(house)
        }
        return houseUpdate
    }

    override fun delete(orgId: String, houseId: String) {
        val query = ("_id" equal ObjectId(houseId)) plus ("orgIndex" equal ObjectId(orgId))
        dbCollection.find(query).firstOrNull() ?: throw NotFoundException("ไม่พบบ้าน id $houseId ให้ลบ")
        dbCollection.deleteMany(query)
    }

    override fun findAll(orgId: String, queryStr: String?, haveLocation: Boolean?, villageName: String?): List<House> {
        val query = "orgIndex" equal ObjectId(orgId)
        when (haveLocation) {
            null -> {
            }
            true -> query.append("location", "\$ne" equal null)
            else -> query.append("location", "\$eq" equal null)
        }

        if (queryStr != null) {
            val regexQuery = Document("\$regex", "^$queryStr").append("\$options", "i")
            val orQuery = BasicBSONList()
            orQuery.add("no" equal regexQuery)
            orQuery.add("villageName" equal regexQuery)
            query.append("\$or", orQuery)
        }
        if (villageName != null) {
            val regexQuery = Document("\$regex", villageName).append("\$options", "i")
            query.append("villageName", regexQuery)
        }

        val house = dbCollection.find(query)
            .sort(Sorts.ascending("villageName", "no"))
            .limit(100)
            .listOf<House>()
        return house.sortedWith(compareBy<House> { it.villageName }
            .thenBy { it.noWithoutTail?.length }
            .thenBy { it.no }
        )
    }

    val House.noWithoutTail
        get() = no?.replace(Regex("^(.+)(/.*)\$"), "$1")

    override fun find(orgId: String, houseId: String): House? {
        logger.debug("Call find in house dao.")
        val house = dbCollection.find("_id" equal ObjectId(houseId))?.first()

        if (house?.get("orgIndex") != ObjectId(orgId)) return null
        return house.toJson()?.parseTo()
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }

    companion object {
        private val logger = getLogger()
    }
}
