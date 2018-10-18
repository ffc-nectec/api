package ffc.airsync.api.services.village

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import me.piruin.geok.geometry.Point
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoVillageDaoTest {
    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: VillageDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer
    val foodShop = Businsess().apply {
        name = "ร้านอาหาร กินจุ"
        businessType = "ร้านอาหารริมทาง"
        location = Point(13.0, 100.3)
        no = "117/8"
    }
    val businsess = Businsess().apply {
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
    fun getFound() {
        val villageInsert = dao.insert(ORG_ID, village)
        val data = dao.get(villageInsert.id)

        data.name `should be equal to` village.name
    }

    @Test
    fun getNotFound() {
        dao.insert(ORG_ID, village)
        val data = dao.get("554d7f5ebc920637b04c7708")
    }
}