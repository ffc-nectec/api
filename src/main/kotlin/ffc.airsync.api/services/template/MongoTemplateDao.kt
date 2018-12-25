package ffc.airsync.api.services.template

import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.listOf
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Template
import org.bson.Document

class MongoTemplateDao(host: String, port: Int) : MongoDao(host, port, "ffc", "template"), TemplateDao {
    override fun insert(template: Template) {
        dbCollection.insertOne(template.toDocument())
    }

    override fun find(query: String): List<Template> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        return dbCollection.find("value" equal regexQuery).listOf()
    }
}
