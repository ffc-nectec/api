package ffc.airsync.api.dao

import com.mongodb.client.model.IndexOptions
import ffc.entity.Lang
import ffc.entity.healthcare.Disease
import org.bson.Document

internal class MongoDiseaseDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "disease"), DiseaseDao {

    init {
        val searchIndex = documentOf(
                "icd10" to "text",
                "name" to "text",
                "translation.th" to "text")
        val insertIndex = documentOf("icd10" to 1)

        try {
            dbCollection.createIndex(searchIndex, IndexOptions().unique(false))
            dbCollection.createIndex(insertIndex, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(disease: Disease): Disease {
        val query = documentOf("icd10" to disease.icd10)
        dbCollection.deleteMany(query)

        dbCollection.insertOne(disease.toDocument())

        return dbCollection.find(query).firstAs()
    }

    override fun find(query: String, lang: Lang) = find(query).translate(lang)

    fun find(query: String): List<Disease> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val listQuery = bsonListOf(
                "translation.th" equal regexQuery,
                "icd10" equal regexQuery,
                "name" equal regexQuery
        )

        return dbCollection.find("\$or" equal listQuery).limit(100).listOf()
    }

    private fun List<Disease>.translate(lang: Lang): List<Disease> {
        return map {
            val nameLang = it.translation[lang] ?: it.name
            val nameEn = it.name
            Disease(
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
        }
    }
}
