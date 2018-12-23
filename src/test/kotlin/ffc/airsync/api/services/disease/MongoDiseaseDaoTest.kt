package ffc.airsync.api.services.disease

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.Lang
import ffc.entity.healthcare.Icd10
import ffc.entity.util.generateTempId
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoDiseaseDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    lateinit var dao: DiseaseDao

    @Before
    fun initDb() {
        dao = MongoDiseaseDao(mongo.address.hostString, mongo.address.port)

        dao.insert(Icd10("Fall", "HHXX001Y").apply { translation[Lang.th] = "อ้วนซ้ำซ้อน" })
        dao.insert(
            Icd10(
                id = generateTempId(),
                name = "Fall2",
                icd10 = "HHXX002Y",
                isNCD = true,
                isChronic = true,
                isEpimedic = true
            ).apply { translation[Lang.th] = "กินไม่หยุด" })
    }

    @Test
    fun query() {
        val disease = dao.find("HHXX002Y").last()

        with(disease) {
            icd10 `should equal` "HHXX002Y"
            isChronic `should equal` true
            isEpimedic `should equal` true
            isNCD `should equal` true
        }
    }

    @Test
    fun getByICD10() {
        dao.getByIcd10("HHXX002Y")!!.name `should be equal to` "Fall2"
    }

    @Test
    fun queryDefaultObject() {
        val disease = dao.find("HHXX001Y").last()

        with(disease) {
            icd10 `should equal` "HHXX001Y"
            isChronic `should equal` false
            isEpimedic `should equal` false
            isNCD `should equal` false
        }
    }

    @Test
    fun insertReturnResult() {
        val result = dao.insert(Icd10("Fall99", "HHXX099Y"))

        result.name `should equal` "Fall99"
    }

    @Test
    fun insertListAndQuery() {
        val icd10List = listOf<Icd10>(
            Icd10("Fall3", "HHXX003T"),
            Icd10("Fall4", "HHXX004T")
        )
        dao.insert(icd10List)

        dao.find("HHXX003T").last().name `should equal` "Fall3"
        dao.find("HHXX004T").last().name `should equal` "Fall4"
    }

    @Test
    fun queryLangTh() {
        val disease = dao.find("HHXX001Y", Lang.th).last()

        disease.name `should equal` "อ้วนซ้ำซ้อน"
    }

    @Test
    fun queryLangEn() {
        val disease = dao.find("HHXX002Y", Lang.en).last()

        disease.name `should equal` "Fall2"
    }
}
