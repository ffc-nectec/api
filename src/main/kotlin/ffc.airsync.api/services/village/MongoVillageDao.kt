package ffc.airsync.api.services.village

import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
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
        val query = "_id" equal ObjectId(village.id)
        val oldDoc = dbCollection.find(query).first()
        require(oldDoc != null) { "ไม่มีข้อมูล Village ที่ต้องการ แก้ไขในระบบ" }
        val villageDoc = village.buildUpdateBson(oldDoc)
        return dbCollection.ffcUpdate(villageDoc)
    }

    override fun get(id: String): Village {
        return dbCollection.find("_id" equal ObjectId(id)).first()?.toJson()?.parseTo<Village>()
            ?: throw NullPointerException("ไม่พบ")
    }

    override fun find(orgId: String, query: String): List<Village> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
