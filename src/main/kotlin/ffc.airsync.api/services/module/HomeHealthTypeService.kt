package ffc.airsync.api.services.module

import ffc.entity.gson.parseTo
import ffc.entity.healthcare.CommunityServiceType
import java.nio.charset.Charset

object HomeHealthTypeService {

    fun query(query: String): List<CommunityServiceType> {
        return homeHealtyTypeDao.find(query)
    }

    fun init() {
        val classloader = Thread.currentThread().contextClassLoader
        val file = classloader.getResourceAsStream("HomeHealthType.json")
            .bufferedReader(Charset.forName("UTF-8"))

        if (query("").count() < 5) {
            val stringData = file.readText()
            val listDisease = stringData.parseTo<List<CommunityServiceType>>()

            homeHealtyTypeDao.insert(listDisease)
        }
    }
}