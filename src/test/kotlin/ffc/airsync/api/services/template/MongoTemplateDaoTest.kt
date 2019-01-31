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
    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    private val ORG_ID2 = "5bbd7f5ebc920637b04c7799"

    lateinit var dao: TemplateDao

    val template1 = Template("ปวดหัวตัวร้อน", "HomeVisit.detail")
    val template2 = Template("เป็นไข้อาเจียน", "HomeVisit.result")

    @Before
    fun setUp() {
        dao = MongoTemplateDao()
    }

    @Test
    fun insert() {
        dao.insert(ORG_ID, template1)
        dao.insert(ORG_ID2, template2)
    }

    @Test
    fun insertList() {
        dao.insert(ORG_ID, ArrayList<Template>().apply {
            add(template1)
            add(template2)
        })
    }

    @Test
    fun insertAndFind() {
        dao.insert(ORG_ID, template1)
        dao.insert(ORG_ID, template2)

        dao.find(ORG_ID, "ตัวร้อน").last().value `should be equal to` "ปวดหัวตัวร้อน"
    }

    @Test
    fun insertListAndFind() {
        dao.insert(ORG_ID, ArrayList<Template>().apply {
            add(template1)
            add(template2)
        })

        dao.find(ORG_ID, "ตัวร้อน").last().value `should be equal to` "ปวดหัวตัวร้อน"
    }

    @Test
    fun delete() {
        dao.insert(ORG_ID, template1)
        dao.insert(ORG_ID2, template2)

        dao.removeByOrgId(ORG_ID)
        dao.find(ORG_ID, "").isEmpty() `should be equal to` true
        dao.find(ORG_ID2, "").isNotEmpty() `should be equal to` true
    }
}
