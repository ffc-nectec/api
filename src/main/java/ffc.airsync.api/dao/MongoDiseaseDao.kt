package ffc.airsync.api.dao

import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.Disease
import org.bson.Document

class MongoDiseaseDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "person"), DiseaseDao {
    override fun insert(disease: Disease): Disease {

        val query = Document()
        query.put("icd10", disease.icd10)

        val docDisease = Document.parse(disease.toJson())

        dbCollection.deleteMany(query)
        dbCollection.insertOne(docDisease)

        val result = dbCollection.find(Document(query)).first()
        val newDisease = result.toJson().parseTo<Disease>()

        return newDisease
    }

    override fun insert(disease: List<Disease>): List<Disease> {
        val newDisease = arrayListOf<Disease>()
        disease.forEach {
            newDisease.add(insert(it))
        }
        return newDisease
    }

    override fun find(query: String): List<Disease> {
        val result = arrayListOf<Disease>()
        val queryDoc = Document()

        queryDoc.put("icd10", query)

        val resultQuery = dbCollection.find(queryDoc)
        resultQuery.forEach {
            val disease = it.toJson().parseTo<Disease>()
            result.add(disease)
        }

        return result
    }
}