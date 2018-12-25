package ffc.airsync.api.services.template

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.Template
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoTemplateDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    lateinit var dao: TemplateDao

    val template1 = Template("ปวดหัวตัวร้อน", "HomeVisit.detail")
    val template2 = Template("เป็นไข้อาเจียน", "HomeVisit.result")

    @Before
    fun setUp() {
        dao = MongoTemplateDao(mongo.address.hostString, mongo.address.port)
    }

    @Test
    fun insert() {
        dao.insert(template1)
        dao.insert(template2)
    }

    @Test
    fun insertList() {
        dao.insert(ArrayList<Template>().apply {
            add(template1)
            add(template2)
        })
    }

    @Test
    fun insertAndFind() {
        dao.insert(template1)
        dao.insert(template2)

        dao.find("ตัวร้อน").last().value `should be equal to` "ปวดหัวตัวร้อน"
    }

    @Test
    fun insertListAndFind() {
        dao.insert(ArrayList<Template>().apply {
            add(template1)
            add(template2)
        })

        dao.find("ตัวร้อน").last().value `should be equal to` "ปวดหัวตัวร้อน"
    }
}
