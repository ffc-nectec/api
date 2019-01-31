package ffc.airsync.api.services.template

import ffc.entity.Template

interface TemplateDao {
    fun insert(orgId: String, template: Template)
    fun insert(orgId: String, template: List<Template>) {
        template.forEach { insert(orgId, it) }
    }

    fun find(orgId: String, query: String): List<Template>
    fun removeByOrgId(orgId: String)
}

val templates: TemplateDao by lazy { MongoTemplateDao() }
