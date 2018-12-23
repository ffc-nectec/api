package ffc.airsync.api.services.disease

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.bsonListOf
import ffc.airsync.api.services.util.documentOf
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.firstAs
import ffc.airsync.api.services.util.listOf
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Lang
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Icd10
import org.bson.Document

internal class MongoDiseaseDao(host: String, port: Int) : MongoDao(host, port, "ffc", "disease"), DiseaseDao {

    init {
        val searchIndex = documentOf(
            "icd10" to "text",
            "name" to "text",
            "translation.th" to "text"
        )
        val insertIndex = documentOf("icd10" to 1)

        try {
            dbCollection.createIndex(searchIndex, IndexOptions().unique(false))
            dbCollection.createIndex(insertIndex, IndexOptions().unique(true))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(disease: Icd10): Icd10 {
        val query = documentOf("icd10" to disease.icd10)
        dbCollection.deleteMany(query)

        dbCollection.insertOne(disease.toDocument())

        return dbCollection.find(query).firstAs()
    }

    override fun find(query: String, lang: Lang) = find(query).translate(lang)

    fun find(query: String): List<Icd10> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val listQuery = bsonListOf(
            "translation.th" equal regexQuery,
            "icd10" equal regexQuery,
            "name" equal regexQuery
        )

        return dbCollection.find("\$or" equal listQuery).limit(100).listOf()
    }

    override fun getByIcd10(icd10: String): Icd10? {
        val query = "icd10" equal icd10
        return dbCollection.find(query).first().toJson().parseTo()
    }

    private fun List<Icd10>.translate(lang: Lang): List<Icd10> {
        return map {
            val nameLang = it.translation[lang] ?: it.name
            val nameEn = it.name
            Icd10(
                id = it.id,
                name = nameLang,
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
