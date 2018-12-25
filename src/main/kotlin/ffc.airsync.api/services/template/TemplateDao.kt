package ffc.airsync.api.services.template

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.entity.Template

interface TemplateDao {
    fun insert(template: Template)
    fun insert(template: List<Template>) {
        template.forEach { insert(it) }
    }

    fun find(query: String): List<Template>
}

val templates: TemplateDao by lazy { MongoTemplateDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
