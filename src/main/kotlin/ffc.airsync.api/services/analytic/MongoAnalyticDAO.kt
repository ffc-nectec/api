package ffc.airsync.api.services.analytic

import com.mongodb.BasicDBObject
import com.mongodb.client.model.UpdateOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.search.Operator
import ffc.airsync.api.services.search.Query
import ffc.airsync.api.services.search.QueryExtractor
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.plus
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.analyze.HealthAnalyzer
import org.bson.BsonDateTime
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import org.joda.time.LocalDate

internal class MongoAnalyticDAO(host: String, port: Int) : AnalyticDAO, MongoDao(host, port, "ffc", "person") {

    override fun insert(
        orgId: String,
        personId: String,
        healthAnalyzer: HealthAnalyzer
    ): HealthAnalyzer {
        val insertDoc = Document.parse(healthAnalyzer.toJson())
        val query = ("_id" equal ObjectId(personId)) plus ("orgIndex" equal ObjectId(orgId))
        dbCollection.updateOne(query, "\$set" equal ("healthAnalyze" equal insertDoc))

        return dbCollection.find(query)
            .projection("healthAnalyze" equal 1)
            .firstOrNull()!!.toJson().parseTo<Person>().healthAnalyze!!
    }

    override fun getByPersonId(orgId: String, personId: String): HealthAnalyzer {
        val query = ("_id" equal ObjectId(personId)) plus ("orgIndex" equal ObjectId(orgId))
        return dbCollection.find(query)
            .projection("healthAnalyze" equal 1)
            .firstOrNull()?.toJson()?.parseTo<Person>()?.healthAnalyze
            ?: throw NoSuchElementException("ไม่พบ Analytic ของ person id $personId")
    }

    override fun getByHouseId(orgId: String, houseId: String): List<Person> {
        val query = ("houseId" equal houseId) plus ("orgIndex" equal ObjectId(orgId))
        val result = dbCollection.find(query)?.projection("healthAnalyze" equal 1)
        return result?.map {
            it.toJson().parseTo<Person>()
        }?.toList() ?: throw NoSuchElementException("ไม่พบ Analytic ของ house id $houseId")
    }

    override fun query(orgId: String, query: String): List<Person> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")

        val queryTextCondition = BasicBSONList().apply {
            add("healthAnalyze.result" equal regexQuery)
        }
        val queryTextReg = "\$or" equal queryTextCondition
        val queryFixOrgIdDoc = "orgIndex" equal ObjectId(orgId)
        val fullQuery = BasicBSONList().apply {
            add(queryFixOrgIdDoc)
            add(queryTextReg)
        }

        return dbCollection.find("\$and" equal fullQuery).limit(20).map {
            it.toJson().parseTo<Person>()
        }.toList()
    }

    override fun deleteByPersonId(orgId: String, personId: String) {
        val set = "healthAnalyze" equal null
        dbCollection.updateOne("_id" equal ObjectId(personId), "\$set" equal set)
    }

    override fun removeByOrgId(orgId: String) {
        val update = BasicDBObject()
        update["\$set"] = BasicDBObject("healthAnalyze", null)

        dbCollection.updateMany("orgIndex" equal ObjectId(orgId), update, UpdateOptions())
    }

    override fun insertBlock(
        orgId: String,
        block: Int,
        healthAnalyzer: Map<String, HealthAnalyzer>
    ): Map<String, HealthAnalyzer> {
        val result = hashMapOf<String, HealthAnalyzer>()

        healthAnalyzer.forEach {
            val query = ("_id" equal ObjectId(it.key)) plus ("orgIndex" equal ObjectId(orgId))
            val insertDoc = Document.parse(it.value.toJson())
            insertDoc["insertBlock"] = block
            dbCollection.updateOne(query, "\$set" equal ("healthAnalyze" equal insertDoc))

            result[it.key] = getByPersonId(orgId, it.key)
        }

        return result
    }

    override fun getBlock(orgId: String, block: Int): Map<String, HealthAnalyzer> {
        val result = hashMapOf<String, HealthAnalyzer>()
        val query = ("healthAnalyze.insertBlock" equal block) plus ("orgIndex" equal ObjectId(orgId))
        dbCollection.find(query).forEach {
            result[it["_id"].toString()] = it["healthAnalyze"]?.toJson()!!.parseTo()
        }
        return result
    }

    override fun confirmBlock(orgId: String, block: Int) {
        val query = ("healthAnalyze.insertBlock" equal block) plus ("orgIndex" equal ObjectId(orgId))
        val update = BasicDBObject()
        update["\$unset"] = BasicDBObject("healthAnalyze.insertBlock", "")

        dbCollection.updateMany(query, update, UpdateOptions())
    }

    override fun unConfirmBlock(orgId: String, block: Int) {
        val query = ("healthAnalyze.insertBlock" equal block) plus ("orgIndex" equal ObjectId(orgId))

        val update = BasicDBObject()
        update["\$set"] = BasicDBObject("healthAnalyze", null)

        dbCollection.updateMany(query, update, UpdateOptions())
    }

    override fun smartQuery(orgId: String, query: String): List<Person> {

        val extractor = QueryExtractor()
        val queryExtractor = extractor.extract(query)
        val mongoQuery = BasicBSONList()
        mongoQuery.add("orgIndex" equal ObjectId(orgId))

        queryExtractor.forEach { key, value ->
            if (key == "age") ageFilter(value, mongoQuery)
            if (key == "dm") {
                if (value.operator == Operator.EQAUL)
                    mongoQuery.add("healthAnalyze.result.DM" equal ("\$exists" equal true))
                else
                    mongoQuery.add("healthAnalyze.result.DM" equal ("\$exists" equal false))
            }
        }

        return dbCollection.find("\$and" equal mongoQuery).limit(50).map {
            it.toJson().parseTo<Person>()
        }?.toList() ?: throw NoSuchElementException("ไม่พบ สิ่งที่ค้้นหา")
    }

    private fun ageFilter(
        value: Query<Any>,
        mongoQuery: BasicBSONList
    ) {
        val minusYears = LocalDate.now().minusYears(value.value.toString().toInt())
        val calDate = BsonDateTime(minusYears.toDate().time)
        when (value.operator) {
            Operator.MORE_THAN -> {
                mongoQuery.add("birthDateMongo" equal ("\$lte" equal calDate))
            }
            Operator.LESS_THEN -> {
                mongoQuery.add("birthDateMongo" equal ("\$gte" equal calDate))
            }
            else -> {
                mongoQuery.add("birthDateMongo" equal ("\$eq" equal calDate))
            }
        }
    }
}
