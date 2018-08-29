package ffc.airsync.api.services.module

import ffc.entity.gson.parseTo
import ffc.entity.healthcare.Disease
import java.io.FileReader

object DiseaseService {

    var deseaseInsert = false

    fun query(query: String): List<Disease> {
        return diseaseDao.find(query)
    }

    fun init() {

        val classloader = Thread.currentThread().contextClassLoader
        val data = classloader.getResource("Disease.json")
        if (!deseaseInsert) {
            deseaseInsert = true
            val data2 = FileReader("${data.file}").readText()
            val listDisease = data2.parseTo<List<Disease>>()

            diseaseDao.insert(listDisease)
        }
    }
}