package ffc.airsync.api.dao

import com.mongodb.client.model.IndexOptions
import ffc.entity.Lang
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.Disease
import org.bson.Document
import org.bson.types.BasicBSONList

internal class MongoDiseaseDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "disease"), DiseaseDao {

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

        dbCollection.deleteMany(query)
        dbCollection.insertOne(docDisease)

        val result = dbCollection.find(query).first()
        val newDisease = result.toJson().parseTo<Disease>()

        return newDisease
    }

    override fun find(query: String): List<Disease> {
        val result = arrayListOf<Disease>()
        val regexQuery = Document("\$regex", query).append("\$options", "i")

        val listQuery = BasicBSONList().apply {
            add(Document("translation.th", regexQuery))
            add(Document("icd10", regexQuery))
            add(Document("name", regexQuery))
        }

        val resultQuery = dbCollection.find(Document("\$or", listQuery)).limit(100)
        resultQuery.forEach {
            val disease = it.toJson().parseTo<Disease>()
            result.add(disease)
        }

        return result
    }

    override fun find(query: String, lang: Lang): List<Disease> {
        val queryResult = find(query)
        val returnResult = translate(queryResult, lang)
        return returnResult
    }

    private fun translate(
        queryResult: List<Disease>,
        lang: Lang
    ): ArrayList<Disease> {
        val returnResult = arrayListOf<Disease>()
        queryResult.forEach {
            val nameLang = it.translation[lang] ?: it.name
            val nameEn = it.name
            val disease = Disease(
                it.id, nameLang,
                icd10 = it.icd10,
                isChronic = it.isChronic,
                isEpimedic = it.isEpimedic,
                isNCD = it.isNCD
            ).apply {
                this.translation.putAll(it.translation)
                this.translation[Lang.en] = nameEn
                this.translation.remove(lang)
            }
            returnResult.add(disease)
        }
        return returnResult
    }
}
