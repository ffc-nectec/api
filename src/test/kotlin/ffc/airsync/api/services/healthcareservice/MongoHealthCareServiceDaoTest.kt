package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.Lang
import ffc.entity.Person
import ffc.entity.ThaiCitizenId
import ffc.entity.User
import ffc.entity.healthcare.BloodPressure
import ffc.entity.healthcare.CommunityService.ServiceType
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import ffc.entity.healthcare.Icd10
import ffc.entity.util.generateTempId
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoHealthCareServiceDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    val ORG_ID = "87543432abcf432123456785"
    lateinit var dao: HealthCareServiceDao
    val comServType = ServiceType(
        "1B0030",
        "ตรวจคัดกรองความเสี่ยง/โรคมะเร็งเต้านมได้ผลปกติ ผู้รับบริการเคยตรวจด้วยตนเองได้ผลปกติ"
    )
    val hypertension = Icd10(
        "Hypertension",
        "i10",
        isEpimedic = false,
        isChronic = true,
        isNCD = true
    ).apply {
        translation.put(Lang.th, "ความดันโลหิต")
    }
    val provider = User(
        "5bbd7f5ebc920637b04c7766",
        "blast",
        "123456",
        User.Role.PROVIDER,
        User.Role.ADMIN
    )
    val patient = Person("5bbd7f5ebc920637b04c7799").apply {
        identities.add(ThaiCitizenId("1154785400590"))
        prename = "Mr."
        firstname = "Piruin"
        lastname = "Panichphol"
    }
    val visit = HealthCareService(provider.id, patient.id, generateTempId()).apply {
        weight = 61.5
        height = 170.0
        bloodPressure = BloodPressure(145.0, 95.0)
        principleDx = hypertension
        respiratoryRate = 24.0
        pulseRate = 72.0
        bodyTemperature = 37.5
        location = Point(14.192390, 120.029384)
        syntom = "ทานอาหารได้น้อย เบื่ออาหาร"
        this.communityServices.add(
            HomeVisit(comServType).apply {
                detail = "ตรวจร่างกายทั่วไป / อธิบานผลเสียของโรค / เปิดโอกาสให้ผู้ป่วยซักถาม"
                result = "ผู้ป่วยเข้าใจเกี่ยวกับโรค สามารถดูแลตัวเองได้และปฎิบัติตามคำแนะนำได้ดี"
                plan = "ติดตามเยี่ยมปีละ 1 ครั้ง"
            }
        )
        nextAppoint = LocalDate.parse("2019-09-21")
    }
    val visit2 = HealthCareService(provider.id, patient.id, generateTempId()).apply {
        weight = 65.0
        height = 170.0
        bloodPressure = BloodPressure(155.0, 99.0)
        principleDx = hypertension
        respiratoryRate = 24.0
        pulseRate = 75.0
        bodyTemperature = 37.5
        location = Point(14.192390, 120.029384)
        syntom = "ทานอาหารได้น้อย เบื่ออาหาร"
        this.communityServices.add(
            HomeVisit(comServType).apply {
                detail = "ตรวจร่างกายทั่วไป / อธิบานผลเสียของโรค / เปิดโอกาสให้ผู้ป่วยซักถาม"
                result = "ผู้ป่วยเข้าใจเกี่ยวกับโรค สามารถดูแลตัวเองได้และปฎิบัติตามคำแนะนำได้ดี"
                plan = "ติดตามเยี่ยมปีละ 1 ครั้ง"
            }
        )
        nextAppoint = LocalDate.parse("2019-09-24")
    }

    @Before
    fun initDb() {
        dao = MongoHealthCareServiceDao()
    }

    @Test
    fun insert() {
        val result = dao.insert(visit, ORG_ID)

        result.height `should equal` visit.height
        /* ktlint-disable */
        (result.communityServices.first() as HomeVisit).result `should equal` "ผู้ป่วยเข้าใจเกี่ยวกับโรค สามารถดูแลตัวเองได้และปฎิบัติตามคำแนะนำได้ดี"
        /* ktlint-enable */
    }

    @Test
    fun find() {
        val result = dao.insert(visit, ORG_ID)
        val find = dao.get(result.id, ORG_ID)

        result.id `should equal` find!!.id
        /* ktlint-disable */
        (find.communityServices.first() as HomeVisit).result `should equal` "ผู้ป่วยเข้าใจเกี่ยวกับโรค สามารถดูแลตัวเองได้และปฎิบัติตามคำแนะนำได้ดี"
        /* ktlint-enable */
    }

    @Test
    fun findByPersonId() {
        dao.insert(visit, ORG_ID)
        dao.insert(visit2, ORG_ID)
        val find = dao.getByPatientId(ORG_ID, patient.id)

        find.size `should be equal to` 2
        find.first().syntom `should equal` visit.syntom
    }

    @Test
    fun insertList() {
        val visitList = arrayListOf<HealthCareService>()
        visitList.add(visit)
        visitList.add(visit2)

        val listResult = dao.insert(visitList, ORG_ID)

        listResult.size `should be equal to` 2
        dao.get(listResult.first().id, ORG_ID)!!.weight `should equal` 61.5
        dao.get(listResult.last().id, ORG_ID)!!.weight `should equal` 65.0
    }

    @Test
    fun update() {
        val visitList = arrayListOf<HealthCareService>()
        visitList.add(visit)
        visitList.add(visit2)

        val healthCareService = dao.insert(visitList, ORG_ID).first()

        healthCareService.pulseRate `should equal` visit.pulseRate
        healthCareService.pulseRate = 90.5

        dao.update(healthCareService, ORG_ID).pulseRate `should equal` 90.5
    }

    @Test
    fun getByOrgId() {
        val insert = dao.insert(visit, ORG_ID)
        dao.insert(visit2, ORG_ID)
        val result = dao.getByOrgId(ORG_ID)

        result.count() `should be equal to` 2
        result.first().id `should be equal to` insert.id
    }

    @Test
    fun delete() {
        dao.insert(visit, ORG_ID)
        dao.insert(visit2, ORG_ID)

        dao.removeByOrgId(ORG_ID)

        dao.getByOrgId(ORG_ID).size `should be equal to` 0
    }

    @Test
    fun deleteDifferenceOrganization() {
        val orgId2 = "87543432abcf432123456786"

        dao.insert(visit, ORG_ID)
        dao.insert(visit2, orgId2)

        dao.removeByOrgId(ORG_ID)

        dao.getByOrgId(ORG_ID).size `should be equal to` 0
        dao.getByOrgId(orgId2).size `should be equal to` 1
    }
}
