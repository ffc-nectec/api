package ffc.airsync.api.services.homehealthtype

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.entity.Lang
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.CommunityService.ServiceType
import org.bson.Document
import org.bson.types.BasicBSONList

internal class MongoHomeHealthTypeDao : MongoDao("ffc", "homeHealthType"),
    HomeHealthTypeDao {
    init {
        createIndexById()
        try {
            dbCollection.createIndex("id" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(homeHealthTypee: ServiceType): ServiceType {
        val query = "id" equal homeHealthTypee.id
        val docHomeHealthType = Document.parse(homeHealthTypee.toJson())
        dbCollection.deleteMany(query)
        dbCollection.insertOne(docHomeHealthType)
        val result = dbCollection.find(query).first()
        result.remove("_id")

        return result.toJson().parseTo()
    }

    private fun findMongo(query: String): List<ServiceType> {
        val result = arrayListOf<ServiceType>()
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val queryDoc = BasicBSONList().apply {
            add("id" equal regexQuery)
            add("name" equal regexQuery)
        }
        val resultQuery = dbCollection.find("\$or" equal queryDoc)

        resultQuery.forEach {
            it.remove("_id")
            val healthMap = it.toJson().parseTo<ServiceType>()
            result.add(healthMap)
        }

        return result
    }

    override fun get(id: String): ServiceType? {
        return dbCollection.find("id" equal id).first()?.toJson()?.parseTo()
    }

    override fun find(query: String): List<ServiceType> {
        val find = findMongo(query)
        val result = groupingResult(find)

        return result.toSet().toList()
    }

    private fun groupingResult(find: List<ServiceType>): ArrayList<ServiceType> {
        val result = arrayListOf<ServiceType>()

        find.forEach {
            val lastMap = find.findLastMap(it) ?: it
            val communityServiceType = ServiceType(lastMap.id, lastMap.name).apply {
                translation[Lang.th] = name
            }
            result.add(communityServiceType)
        }
        return result
    }

    private fun List<ServiceType>.findLastMap(type: ServiceType): ServiceType? {
        return this.find {
            try {
                it.id == type.link!!.keys["map"]
            } catch (ignore: Exception) {
                false
            }
        }
    }
}
