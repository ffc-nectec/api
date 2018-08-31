package ffc.airsync.api.services.module

import ffc.entity.gson.parseTo
import ffc.entity.healthcare.CommunityServiceType
import java.io.FileReader

object HomeHealthTypeService {

    fun query(query: String): List<CommunityServiceType> {
        return homeHealtyTypeDao.find(query)
    }

    fun init() {
        val classloader = Thread.currentThread().contextClassLoader
        val data = classloader.getResource("HomeHealthType.json")

        if (query("").count() < 5) {
            val data2 = FileReader("${data.file}").readText()
            val listDisease = data2.parseTo<List<Map<String, String>>>()

            homeHealtyTypeDao.insert(listDisease)
        }
    }
}