package ffc.airsync.api.services.village

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.Village
import ffc.entity.place.Business
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoVillageDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: VillageDao
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
    val nectecVillage = Village().apply {
        name = "หมู่บ้าน Nectec"
        places.add(foodShop)
        places.add(businsess)
    }
    val catVillage = Village().apply {
        name = "หมู่บ้าน Cat"
        places.add(foodShop)
        places.add(businsess)
    }
    val rabbitVillage = Village().apply {
        name = "หมู่บ้าน Rabbit"
        places.add(foodShop)
        places.add(businsess)
    }

    @Before
    fun setUp() {
        dao = MongoVillageDao()
    }

    @Test
    fun insert() {
        val villageInsert = dao.insert(ORG_ID, nectecVillage)

        villageInsert.name `should be equal to` nectecVillage.name
        villageInsert.isTempId `should be equal to` false
        villageInsert.places.first().type `should be equal to` "Business"
    }

    @Test
    fun insertList() {
        val result = dao.insert(ORG_ID, listOf(nectecVillage, catVillage, rabbitVillage))

        result.size `should be equal to` 3
    }

    @Test
    fun update() {
        val villageInsert = dao.insert(ORG_ID, nectecVillage)

        villageInsert.name = "หมู่บ้าน หมีน้อย"
        villageInsert.places.removeIf { it.no == "117/8" }
        val villageUpdate = dao.update(ORG_ID, villageInsert)

        villageInsert.id `should be equal to` villageUpdate.id
        villageUpdate.name `should be equal to` "หมู่บ้าน หมีน้อย"
    }

    @Test(expected = java.lang.NullPointerException::class)
    fun deleteFound() {
        val villageInsert = dao.insert(ORG_ID, nectecVillage)

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
        dao.insert(ORG_ID, nectecVillage)

        try {
            dao.delete(ORG_ID, ORG_ID)
        } catch (ex: java.lang.Exception) {
            ex.message!! `should be equal to` "ไม่พบข้อมูลสำหรับการลบ"
            throw ex
        }
    }

    @Test
    fun getFound() {
        val villageInsert = dao.insert(ORG_ID, nectecVillage)
        val data = dao.get(ORG_ID, villageInsert.id)

        data.name `should be equal to` nectecVillage.name
    }

    @Test(expected = java.lang.NullPointerException::class)
    fun getNotFound() {
        dao.insert(ORG_ID, nectecVillage)

        try {
            dao.get(ORG_ID, "554d7f5ebc920637b04c7708")
        } catch (ex: java.lang.Exception) {
            ex.message!! `should be equal to` "ค้นหาข้อมูลที่ต้องการไม่พบ ข้อมูลอาจถูกลบ หรือ ใส่ข้อมูลอ้างอิงผิด"
            throw ex
        }
    }

    @Test
    fun findVillageName() {
        dao.insert(ORG_ID, nectecVillage)
        val find = dao.find(ORG_ID, "Nectec").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun findPlacesName() {
        dao.insert(ORG_ID, nectecVillage)
        val find = dao.find(ORG_ID, "Nectec")

        find.first().name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun findPlancesNo() {
        dao.insert(ORG_ID, nectecVillage)
        val find = dao.find(ORG_ID, "117/8").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test(expected = java.util.NoSuchElementException::class)
    fun findFail() {
        dao.insert(ORG_ID, nectecVillage)
        val find = dao.find(ORG_ID, "xxaabb").first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun findOrgId() {
        dao.insert(ORG_ID, nectecVillage)
        val find = dao.find(ORG_ID).first()

        find.name `should be equal to` "หมู่บ้าน Nectec"
    }

    @Test
    fun removeByOrgId() {
        dao.insert(ORG_ID, nectecVillage)
        dao.removeByOrgId(ORG_ID)

        dao.find(ORG_ID).firstOrNull() `should equal` null
    }
}
