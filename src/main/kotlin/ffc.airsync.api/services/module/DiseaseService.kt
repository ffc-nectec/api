package ffc.airsync.api.services.module

import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Disease
import java.nio.charset.Charset

object DiseaseService {

    fun query(query: String): List<Disease> {
        return diseaseDao.find(query)
    }

    fun init() {

        val classloader = Thread.currentThread().contextClassLoader
        val data = classloader.getResourceAsStream("Disease.json")
            .bufferedReader(Charset.forName("UTF-8"))

        if (query("").count() < 5) {
            val data2 = data.readText()
            val listDisease = data2.parseTo<List<Disease>>()

            diseaseDao.insert(listDisease)
        }
    }
}