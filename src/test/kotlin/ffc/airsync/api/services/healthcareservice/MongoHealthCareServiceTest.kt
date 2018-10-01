package ffc.airsync.api.services.healthcareservice

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.dao.MongoAbsConnect
import ffc.entity.Lang
import ffc.entity.Person
import ffc.entity.ThaiCitizenId
import ffc.entity.User
import ffc.entity.healthcare.BloodPressure
import ffc.entity.healthcare.CommunityServiceType
import ffc.entity.healthcare.Disease
import ffc.entity.healthcare.HomeVisit
import ffc.entity.healthcare.homeVisit
import ffc.entity.util.generateTempId
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoHealthCareServiceTest {
    lateinit var dao: HealthCareServiceDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer
    val comServType = CommunityServiceType(
        "1B0030",
        "ตรวจคัดกรองความเสี่ยง/โรคมะเร็งเต้านมได้ผลปกติ ผู้รับบริการเคยตรวจด้วยตนเองได้ผลปกติ"
    )
    val hypertension = Disease(
        "id2h3",
        "Hypertension",
        "i10",
        isEpimedic = false,
        isChronic = true,
        isNCD = true
    ).apply {
        translation.put(Lang.th, "ความดันโลหิต")
    }
    val provider = User(
        generateTempId(),
        "blast",
        "123456",
        User.Role.PROVIDER,
        User.Role.ADMIN
    )
    val patient = Person().apply {
        identities.add(ThaiCitizenId("1154785400590"))
        prename = "Mr."
        firstname = "Piruin"
        lastname = "Panichphol"
    }
    val visit = provider.homeVisit(patient.id, comServType).apply {
        weight = 61.5
        height = 170.0
        bloodPressure = BloodPressure(145.0, 95.0)
        principleDx = hypertension
        respiratoryRate = 24.0
        pulseRate = 72.0
        bodyTemperature = 37.5
        location = Point(14.192390, 120.029384)
        syntom = "ทานอาหารได้น้อย เบื่ออาหาร"
        detail = "ตรวจร่างกายทั่วไป / อธิบานผลเสียของโรค / เปิดโอกาสให้ผู้ป่วยซักถาม"
        result = "ผู้ป่วยเข้าใจเกี่ยวกับโรค สามารถดูแลตัวเองได้และปฎิบัติตามคำแนะนำได้ดี"
        plan = "ติดตามเยี่ยมปีละ 1 ครั้ง"
        nextAppoint = LocalDate.parse("2019-09-21")
    }

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = MongoHealthCareServiceDao(serverAddress.hostString, serverAddress.port)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insert() {
        val result = dao.insert(visit, "23232324ddef")

        result.height `should equal` visit.height
        (result as HomeVisit).nextAppoint `should equal` LocalDate.parse("2019-09-21")
    }

    @Test
    fun find() {
        val result = dao.insert(visit, "abxxa")
        val find = dao.find(result.id, "abxxa")

        result.id `should equal` find!!.id
        (find as HomeVisit).nextAppoint `should equal` LocalDate.parse("2019-09-21")
    }

    @Test
    fun findByPersonId() {
        val result = dao.insert(visit, "abxxa")
        val find = dao.findByPatientId(result.patientId, "abxxa")

        find.size `should be equal to` 1
        find.first().syntom `should equal` result.syntom
    }

    @Test
    fun get() {
        val insert = dao.insert(visit, "23232324ddef")
        val result = dao.get("23232324ddef")

        result.count() `should be equal to` 1
        result.first().id `should be equal to` insert.id
    }
}
