package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.healthcare.CommunityService.ServiceType
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoHomeHealthTypeDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    lateinit var dao: HomeHealthTypeDao

    @Before
    fun initDb() {
        dao = MongoHomeHealthTypeDao()

        dao.insert(ServiceType("1A001", "เยี่ยมผู้ป่วยโรคเบาหวาน "))
        dao.insert(ServiceType("1D01300", "ให้ทันตสุขศึกษาหญิงตั้งครรภ์"))
    }

    @Test
    fun insertReturnResult() {
        val result = dao.insert(
            ServiceType(
                "1E11",
                "การตรวจคัดกรองภาวะอ้วนในประชาชนอายุ 15 ปีขึ้นไป โดยการวัดเส้นรอบเอว หรือประเมินค่าดัชนีมวลกาย"
            )
        )

        result.id `should equal` "1E11"
    }

    @Test
    fun findByCode() {
        val find = dao.find("1A").first()

        find.id `should equal` "1A001"
        find.name `should equal` "เยี่ยมผู้ป่วยโรคเบาหวาน "
    }

    @Test
    fun findByMean() {
        val find = dao.find("สุข").first()

        find.id `should equal` "1D01300"
        find.name `should equal` "ให้ทันตสุขศึกษาหญิงตั้งครรภ์"
    }
}
