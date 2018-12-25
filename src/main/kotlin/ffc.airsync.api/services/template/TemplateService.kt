package ffc.airsync.api.services.template

import ffc.airsync.api.getResourceAs
import ffc.entity.Template

object TemplateService {
    fun init() {
        if (templates.find("").count() < 5) {
            templates.insert(getResourceAs<List<Template>>("template.json"))
        }
    }
}
