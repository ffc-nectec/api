package ffc.airsync.api.services.specialpp

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.bsonListOf
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.listOf
import ffc.airsync.api.services.util.toDocument
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.SpecialPP
import org.bson.Document

class MongoSpecialPpType(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "specialpp"), SpecialPpDao {

    init {
        dbCollection.createIndex("id" equal 1, IndexOptions().unique(true))
    }

    override fun insert(ppType: SpecialPP.PPType) {
        dbCollection.deleteMany("id" equal ppType.id)
        dbCollection.insertOne(ppType.toDocument())
    }

    override fun get(id: String): SpecialPP.PPType {
        return dbCollection.find("id" equal id)?.first()?.toJson()?.parseTo()
            ?: throw NoSuchElementException("ไม่พบ PPType $id")
    }

    override fun query(query: String): List<SpecialPP.PPType> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val listQuery = bsonListOf(
            "translation.th" equal regexQuery,
            "id" equal regexQuery,
            "name" equal regexQuery
        )

        return dbCollection.find("\$or" equal listQuery).limit(100).listOf()
    }
}
