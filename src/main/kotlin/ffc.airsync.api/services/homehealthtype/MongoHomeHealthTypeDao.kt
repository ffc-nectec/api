package ffc.airsync.api.services.homehealthtype

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.equal
import ffc.entity.Lang
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.CommunityServiceType
import org.bson.Document
import org.bson.types.BasicBSONList

internal class MongoHomeHealthTypeDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "homeHealthType"),
    HomeHealthTypeDao {
    init {
        try {
            dbCollection.createIndex("id" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(homeHealthTypee: CommunityServiceType): CommunityServiceType {
        val query = "id" equal homeHealthTypee.id
        val docHomeHealthType = Document.parse(homeHealthTypee.toJson())
        dbCollection.deleteMany(query)
        dbCollection.insertOne(docHomeHealthType)
        val result = dbCollection.find(query).first()
        result.remove("_id")

        return result.toJson().parseTo()
    }

    private fun findMongo(query: String): List<CommunityServiceType> {
        val result = arrayListOf<CommunityServiceType>()
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val queryDoc = BasicBSONList().apply {
            add("id" equal regexQuery)
            add("name" equal regexQuery)
        }
        val resultQuery = dbCollection.find("\$or" equal queryDoc)

        resultQuery.forEach {
            it.remove("_id")
            val healthMap = it.toJson().parseTo<CommunityServiceType>()
            result.add(healthMap)
        }

        return result
    }

    override fun find(query: String): List<CommunityServiceType> {
        val find = findMongo(query)
        val result = groupingResult(find)

        return result.toSet().toList()
    }

    private fun groupingResult(find: List<CommunityServiceType>): ArrayList<CommunityServiceType> {
        val result = arrayListOf<CommunityServiceType>()

        find.forEach {
            val lastMap = find.findLastMap(it) ?: it
            val communityServiceType = CommunityServiceType(lastMap.id, lastMap.name).apply {
                translation[Lang.th] = name
            }
            result.add(communityServiceType)
        }
        return result
    }

    private fun List<CommunityServiceType>.findLastMap(type: CommunityServiceType): CommunityServiceType? {
        return this.find {
            try {
                it.id == type.link!!.keys["map"]
            } catch (ignore: Exception) {
                false
            }
        }
    }
}
