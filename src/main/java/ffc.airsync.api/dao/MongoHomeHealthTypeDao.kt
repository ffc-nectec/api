package ffc.airsync.api.dao

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.printDebug
import ffc.entity.Lang
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.CommunityServiceType
import org.bson.Document
import org.bson.types.BasicBSONList

internal class MongoHomeHealthTypeDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "homeHealthType"),
    HomeHealthTypeDao {

    init {
        val insertIndex = Document("code", 1)
        try {
            dbCollection.createIndex(insertIndex, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(homeHealthTypee: Map<String, String>): Map<String, String> {
        val query = Document("code", homeHealthTypee["code"])

        val docHomeHealthType = Document.parse(homeHealthTypee.toJson())
        dbCollection.deleteMany(query)
        dbCollection.insertOne(docHomeHealthType)

        val result = dbCollection.find(query).first()
        result.remove("_id")

        return result.toJson().parseTo()
    }

    override fun insert(homeHealthTypee: List<Map<String, String>>): List<Map<String, String>> {
        val result = arrayListOf<Map<String, String>>()
        var count = 1
        val countAll = homeHealthTypee.count()
        homeHealthTypee.forEach {
            printDebug("Insert home health type A:=$countAll P:${count++}")
            result.add(insert(it))
        }
        return result
    }

    private fun findMongo(query: String): List<Map<String, String>> {

        val result = arrayListOf<Map<String, String>>()
        val regexQuery = Document("\$regex", query).append("\$options", "i")

        val query = BasicBSONList().apply {
            add(Document("code", regexQuery))
            add(Document("mean", regexQuery))
            add(Document("map", regexQuery))
        }

        val resultQuery = dbCollection.find(Document("\$or", query)).limit(20)

        resultQuery.forEach {
            it.remove("_id")
            val healthMap = it.toJson().parseTo<Map<String, String>>()
            result.add(healthMap)
        }

        return result
    }

    override fun find(query: String): List<CommunityServiceType> {
        val find = findMongo(query)
        val result = arrayListOf<CommunityServiceType>()

        find.forEach {
            val id = it["code"]
            val name = it["mean"]

            if ((id != null) && (name != null)) {
                val communityServiceType = CommunityServiceType(id, name).apply {
                    translation[Lang.th] = name
                }
                result.add(communityServiceType)
            }
        }
        return result
    }
}