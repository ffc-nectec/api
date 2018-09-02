package ffc.airsync.api.services.module

import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Disease
import java.io.FileReader

object DiseaseService {

    fun query(query: String): List<Disease> {
        return diseaseDao.find(query)
    }

    fun init() {

        val classloader = Thread.currentThread().contextClassLoader
        val data = classloader.getResource("Disease.json")
        if (query("").count() < 5) {
            val data2 = FileReader("${data.file}").readText()
            val listDisease = data2.parseTo<List<Disease>>()

            diseaseDao.insert(listDisease)
        }
    }
}