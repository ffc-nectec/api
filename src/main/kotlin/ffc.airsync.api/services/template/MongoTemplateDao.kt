package ffc.airsync.api.services.template

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.listOf
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Template
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

class MongoTemplateDao(host: String, port: Int) : MongoDao(host, port, "ffc", "template"), TemplateDao {

    init {
        dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
    }

    override fun insert(orgId: String, template: Template) {
        val bsonDoc = template.toDocument()
        bsonDoc.append("orgIndex", ObjectId(orgId))
        dbCollection.insertOne(bsonDoc)
    }

    override fun find(orgId: String, query: String): List<Template> {
        val bsonQuery = BasicBSONList()
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        bsonQuery.add("orgIndex" equal ObjectId(orgId))
        bsonQuery.add("value" equal regexQuery)

        return dbCollection.find("\$and" equal bsonQuery).listOf()
    }

    override fun removeByOrgId(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }
}
