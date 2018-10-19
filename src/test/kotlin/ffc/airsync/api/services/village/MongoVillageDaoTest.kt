package ffc.airsync.api.services.village

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
import ffc.entity.Village
import ffc.entity.place.Business
import me.piruin.geok.geometry.Point
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoVillageDaoTest {
    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: VillageDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer
    val foodShop = Business().apply {
        name = "ร้านอาหาร กินจุ"
        businessType = "ร้านอาหารริมทาง"
        location = Point(13.0, 100.3)
        no = "117/8"
    }
    val businsess = Business().apply {
        name = "บ้านเช่า นายนามี คมคนมา"
        businessType = "อาคารปล่อยเช่า"
        location = Point(13.009, 100.3453)
    }
    val village = Village().apply {
        name = "หมู่บ้าน Nectec"
        places.add(foodShop)
        places.add(businsess)
    }

    @Before
    fun setUp() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = MongoVillageDao(serverAddress.hostString, serverAddress.port)
    }

    @After
    fun tearDown() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insert() {
        val villageInsert = dao.insert(ORG_ID, village)

        villageInsert.name `should be equal to` village.name
        villageInsert.isTempId `should be equal to` false
        villageInsert.places.first().type `should be equal to` "Business"
    }

    @Test
    fun update() {
        val villageInsert = dao.insert(ORG_ID, village)

        villageInsert.name = "หมู่บ้าน หมีน้อย"
        villageInsert.places.removeIf { it.no == "117/8" }
        val villageUpdate = dao.update(ORG_ID, villageInsert)

        villageInsert.id `should be equal to` villageUpdate.id
        villageUpdate.name `should be equal to` "หมู่บ้าน หมีน้อย"
    }

    @Test(expected = java.lang.NullPointerException::class)
    fun deleteFound() {
        val villageInsert = dao.insert(ORG_ID, village)

        dao.delete(ORG_ID, villageInsert.id)
        try {
            dao.get(ORG_ID, villageInsert.id)
        } catch (ex: Exception) {
            ex.message!! `should be equal to` "ค้นหาข้อมูลที่ต้องการไม่พบ ข้อมูลอาจถูกลบ หรือ ใส่ข้อมูลอ้างอิงผิด"
            throw ex
        }
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun deleteNotFound() {
        dao.insert(ORG_ID, village)

        try {
            dao.delete(ORG_ID, ORG_ID)
        } catch (ex: java.lang.Exception) {
            ex.message!! `should be equal to` "ไม่พบข้อมูลสำหรับการลบ"
            throw ex
        }
    }

    @Test
    fun getFound() {
        val villageInsert = dao.insert(ORG_ID, village)
        val data = dao.get(ORG_ID, villageInsert.id)

        data.name `should be equal to` village.name
    }

    @Test(expected = java.lang.NullPointerException::class)
    fun getNotFound() {
        dao.insert(ORG_ID, village)

        try {
            dao.get(ORG_ID, "554d7f5ebc920637b04c7708")
        } catch (ex: java.lang.Exception) {
            ex.message!! `should be equal to` "ค้นหาข้อมูลที่ต้องการไม่พบ ข้อมูลอาจถูกลบ หรือ ใส่ข้อมูลอ้างอิงผิด"
            throw ex
        }
    }

    @Test
    fun findVillageName() {
        dao.insert(ORG_ID, village)
        val find = dao.find(ORG_ID, "Nectec").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun findPlacesName() {
        dao.insert(ORG_ID, village)
        val find = dao.find(ORG_ID, "บ้านเช่า").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun findPlancesNo() {
        dao.insert(ORG_ID, village)
        val find = dao.find(ORG_ID, "117/8").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test(expected = java.util.NoSuchElementException::class)
    fun findFail() {
        dao.insert(ORG_ID, village)
        val find = dao.find(ORG_ID, "xxaabb").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun findOrgId() {
        dao.insert(ORG_ID, village)
        val find = dao.find(ORG_ID).first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }
}
