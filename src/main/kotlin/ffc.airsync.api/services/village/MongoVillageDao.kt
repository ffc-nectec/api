package ffc.airsync.api.services.village

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.TextFindMongo
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildQueryDoc
import ffc.airsync.api.services.util.buildTextFindMongo
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.ffcUpdate
import ffc.entity.Village
import ffc.entity.gson.parseTo
import org.bson.types.ObjectId

class MongoVillageDao(host: String, port: Int) : VillageDao, MongoDao(host, port, "ffc", "village") {

    init {
        dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
    }

    override fun insert(orgId: String, village: Village): Village {
        val villageDoc = village.buildInsertBson()
        villageDoc.append("orgIndex", ObjectId(orgId))

        return dbCollection.ffcInsert(villageDoc)
    }

    override fun update(orgId: String, village: Village): Village {
        val oldDoc = dbCollection.find(village.buildQueryDoc()).first()
        require(oldDoc != null) { "ไม่มีข้อมูล Village ที่ต้องการ แก้ไขในระบบ" }
        val villageDoc = village.buildUpdateBson()
        return dbCollection.ffcUpdate(villageDoc)
    }

    override fun delete(orgId: String, id: String) {
        val query = id.buildQueryDoc()
        require((dbCollection.find(query).first()?.get("orgIndex").toString()) == orgId) { "ไม่พบข้อมูลสำหรับการลบ" }

        dbCollection.deleteOne(query)
    }

    override fun get(orgId: String, id: String): Village {
        val villageDoc = dbCollection.find(id.buildQueryDoc()).first()
            ?: throw NullPointerException("ค้นหาข้อมูลที่ต้องการไม่พบ ข้อมูลอาจถูกลบ หรือ ใส่ข้อมูลอ้างอิงผิด")

        require(villageDoc["orgIndex"].toString() == orgId) {
            "ค้นหาข้อมูลที่ต้องการไม่พบ ข้อมูลอาจถูกลบ หรือ ใส่ข้อมูลอ้างอิงผิด"
        }

        return villageDoc.toJson()!!.parseTo()
    }

    override fun find(orgId: String, query: String): List<Village> {
        val stringQuery: TextFindMongo = { arrayListOf("name", "places.name", "places.no") }
        val queryObj = query.buildTextFindMongo(orgId, queryField = stringQuery)
        val resultQuery = dbCollection.find(queryObj).limit(20)

        return resultQuery.map { it.toJson().parseTo<Village>() }.toList()
    }

    override fun find(orgId: String): List<Village> {
        val findDoc = dbCollection.find("orgIndex" equal ObjectId(orgId))
            ?: throw java.util.NoSuchElementException("ไม่พบข้อมูลการค้นหา")

        return findDoc.map { it.toJson().parseTo<Village>() }.toList()
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}
