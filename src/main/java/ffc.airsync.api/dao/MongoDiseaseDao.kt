package ffc.airsync.api.dao

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.printDebug
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.Disease
import org.bson.Document
import org.bson.types.BasicBSONList

class MongoDiseaseDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "disease"), DiseaseDao {

    init {
        val searchIndex = Document("icd10", "text")
        searchIndex.append("name", "text")
        searchIndex.append("translation.th", "text")
        val insertIndex = Document("icd10", 1)

        try {
            dbCollection.createIndex(searchIndex, IndexOptions().unique(false))
            dbCollection.createIndex(insertIndex, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(disease: Disease): Disease {
        val query = Document()
        query.put("icd10", disease.icd10)

        val docDisease = Document.parse(disease.toJson())
        // docDisease.append("name-th", disease.translation[Lang.th])

        dbCollection.deleteMany(query)
        dbCollection.insertOne(docDisease)

        val result = dbCollection.find(Document(query)).first()
        val newDisease = result.toJson().parseTo<Disease>()

        return newDisease
    }

    override fun insert(disease: List<Disease>): List<Disease> {
        var count = 1
        val countAll = disease.count()
        val newDisease = arrayListOf<Disease>()
        disease.forEach {
            printDebug("Insert A:=$countAll P:${count++}")
            newDisease.add(insert(it))
        }
        return newDisease
    }

    override fun find(query: String): List<Disease> {
        val result = arrayListOf<Disease>()
        val listQuery = BasicBSONList().apply {

            add(Document("translation.th", Document("\$regex", query).append("\$options", "i")))
            add(Document("icd10", Document("\$regex", query).append("\$options", "i")))
            add(Document("name", Document("\$regex", query).append("\$options", "i")))
        }

        val resultQuery = dbCollection.find(Document("\$or", listQuery)).limit(20)
        resultQuery.forEach {
            val disease = it.toJson().parseTo<Disease>()
            result.add(disease)
        }

        return result
    }
}