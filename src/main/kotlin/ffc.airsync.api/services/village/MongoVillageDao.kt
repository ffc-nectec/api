package ffc.airsync.api.services.village

import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildQueryDoc
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.ffcUpdate
import ffc.entity.Village
import ffc.entity.gson.parseTo
import org.bson.types.ObjectId

class MongoVillageDao(host: String, port: Int) : VillageDao, MongoAbsConnect(host, port, "ffc", "village") {
    override fun insert(orgId: String, village: Village): Village {
        val villageDoc = village.buildInsertBson()
        villageDoc.append("orgId", ObjectId(orgId))

        return dbCollection.ffcInsert(villageDoc)
    }

    override fun update(orgId: String, village: Village): Village {
        val oldDoc = dbCollection.find(village.buildQueryDoc()).first()
        require(oldDoc != null) { "ไม่มีข้อมูล Village ที่ต้องการ แก้ไขในระบบ" }
        val villageDoc = village.buildUpdateBson(oldDoc)
        return dbCollection.ffcUpdate(villageDoc)
    }

    override fun delete(orgId: String, id: String) {
        val query = id.buildQueryDoc()
        require((dbCollection.find(query).first()?.get("orgId").toString()) == orgId) { "ไม่พบข้อมูลสำหรับการลบ" }

        dbCollection.deleteOne(query)
    }

    override fun get(id: String): Village {
        return dbCollection.find(id.buildQueryDoc()).first()?.toJson()?.parseTo<Village>()
            ?: throw NullPointerException("ค้นหาข้อมูลที่ต้องการไม่พบ ข้อมูลอาจถูกลบ หรือ ใส่ข้อมูลอ้างอิงผิด")
    }

    override fun find(orgId: String, query: String): List<Village> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
