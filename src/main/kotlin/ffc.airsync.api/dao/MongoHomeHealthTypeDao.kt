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
        val insertIndex = Document("id", 1)
        try {
            dbCollection.createIndex(insertIndex, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(homeHealthTypee: CommunityServiceType): CommunityServiceType {
        val query = Document("id", homeHealthTypee.id)

        val docHomeHealthType = Document.parse(homeHealthTypee.toJson())
        dbCollection.deleteMany(query)
        dbCollection.insertOne(docHomeHealthType)

        val result = dbCollection.find(query).first()
        result.remove("_id")

        return result.toJson().parseTo()
    }

    override fun insert(homeHealthTypee: List<CommunityServiceType>): List<CommunityServiceType> {
        val result = arrayListOf<CommunityServiceType>()
        var count = 1
        val countAll = homeHealthTypee.count()
        homeHealthTypee.forEach {
            printDebug("Insert home health type A:=$countAll P:${count++}")
            result.add(insert(it))
        }
        return result
    }

    private fun findMongo(query: String): List<CommunityServiceType> {

        val result = arrayListOf<CommunityServiceType>()
        val regexQuery = Document("\$regex", query).append("\$options", "i")

        val query = BasicBSONList().apply {
            add(Document("id", regexQuery))
            add(Document("name", regexQuery))
        }

        val resultQuery = dbCollection.find(Document("\$or", query)).limit(100)

        resultQuery.forEach {
            it.remove("_id")
            val healthMap = it.toJson().parseTo<CommunityServiceType>()
            result.add(healthMap)
        }

        return result
    }

    override fun find(query: String): List<CommunityServiceType> {

        val find = findMongo(query)
        val result = arrayListOf<CommunityServiceType>()

        find.forEach {

            val it = find.findLastMap(it) ?: it

            val communityServiceType = CommunityServiceType(it.id, it.name).apply {
                translation[Lang.th] = name
            }
            result.add(communityServiceType)
        }

        return result.toSet().toList()
    }

    fun List<CommunityServiceType>.findLastMap(communityServiceType: CommunityServiceType): CommunityServiceType? {
        return this.find {
            try {
                it.id == communityServiceType.link!!.keys["map"]
            } catch (ignore: Exception) {
                false
            }
        }
    }
}
