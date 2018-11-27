package ffc.airsync.api.services.analytic

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.equal
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.analyze.HealthAnalyzer
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

internal class MongoAnalyticDAO(host: String, port: Int) : AnalyticDAO, MongoAbsConnect(host, port, "ffc", "analytic") {

    init {
        dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
        dbCollection.createIndex("houseId" equal 1, IndexOptions().unique(false))
        dbCollection.createIndex("personId" equal 1, IndexOptions().unique(true))
    }

    override fun insert(
        orgId: String,
        personId: String,
        houseId: String,
        healthAnalyzer: HealthAnalyzer
    ): HealthAnalyzer {
        val insertDoc = Document.parse(healthAnalyzer.toJson())
        insertDoc.append("orgIndex", ObjectId(orgId))
        insertDoc.append("personId", ObjectId(personId))
        insertDoc.append("houseId", ObjectId(houseId))
        dbCollection.insertOne(insertDoc)
        return dbCollection.find("personId" equal ObjectId(personId)).firstOrNull()!!.toJson().parseTo()
    }

    override fun getByPersonId(orgId: String, personId: String): HealthAnalyzer {
        return dbCollection.find("personId" equal ObjectId(personId))
            .firstOrNull()?.toJson()?.parseTo()
            ?: throw NoSuchElementException("ไม่พบ Analytic ของ person id $personId")
    }

    override fun getByHouseId(orgId: String, houseId: String): List<HealthAnalyzer> {
        return dbCollection.find("houseId" equal ObjectId(houseId))?.map {
            it.toJson().parseTo<HealthAnalyzer>()
        }?.toList() ?: throw NoSuchElementException("ไม่พบ Analytic ของ house id $houseId")
    }

    override fun query(orgId: String, query: String): HashMap<String, HealthAnalyzer> {
        val result = hashMapOf<String, HealthAnalyzer>()

        val regexQuery = Document("\$regex", query).append("\$options", "i")

        val queryTextCondition = BasicBSONList().apply {
            add("result" equal regexQuery)
        }
        val queryTextReg = "\$or" equal queryTextCondition
        val queryFixOrgIdDoc = "orgIndex" equal ObjectId(orgId)
        val fullQuery = BasicBSONList().apply {
            add(queryFixOrgIdDoc)
            add(queryTextReg)
        }

        dbCollection.find("\$and" equal fullQuery).limit(20).forEach {
            result[it["personId"].toString()] = it.toJson().parseTo<HealthAnalyzer>()
        }

        return result
    }

    override fun deleteByPersonId(orgId: String, personId: String) {
        dbCollection.deleteMany("personId" equal ObjectId(personId))
    }

    override fun deleteByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}
