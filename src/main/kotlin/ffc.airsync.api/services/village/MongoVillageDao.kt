package ffc.airsync.api.services.village

import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import org.bson.types.ObjectId

class MongoVillageDao(host: String, port: Int) : VillageDao, MongoAbsConnect(host, port, "ffc", "village") {
    override fun insert(orgId: String, village: Village): Village {
        val villageDoc = village.buildInsertBson()
        villageDoc.append("orgId", ObjectId(orgId))

        return dbCollection.ffcInsert(villageDoc)
    }

    override fun update(orgId: String, village: Village): Village {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(id: String): Village {
        return dbCollection.find("_id" equal ObjectId(id)).first()?.toJson()?.parseTo<Village>()
            ?: throw NullPointerException("ไม่พบ")
    }

    override fun find(orgId: String, query: String): List<Village> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
