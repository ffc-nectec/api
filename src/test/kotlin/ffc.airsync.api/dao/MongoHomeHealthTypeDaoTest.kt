package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.healthcare.CommunityServiceType
import org.amshove.kluent.`should equal`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoHomeHealthTypeDaoTest {
    lateinit var dao: HomeHealthTypeDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = homeHealthTypes(serverAddress.hostString, serverAddress.port)

        dao.insert(CommunityServiceType("1A001", "เยี่ยมผู้ป่วยโรคเบาหวาน "))
        dao.insert(CommunityServiceType("1D01300", "ให้ทันตสุขศึกษาหญิงตั้งครรภ์"))
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insertReturnResult() {

        val result = dao.insert(
            CommunityServiceType(
                "1E11",
                "การตรวจคัดกรองภาวะอ้วนในประชาชนอายุ 15 ปีขึ้นไป โดยการวัดเส้นรอบเอว หรือประเมินค่าดัชนีมวลกาย"
            )
        )

        result.id `should equal` "1E11"
    }

    @Test
    fun findByCode() {
        dao.find("1A").first().id `should equal` "1A001"
        dao.find("1A").first().name `should equal` "เยี่ยมผู้ป่วยโรคเบาหวาน "
    }

    @Test
    fun findByMean() {
        dao.find("สุข").first().id `should equal` "1D01300"
        dao.find("สุข").first().name `should equal` "ให้ทันตสุขศึกษาหญิงตั้งครรภ์"
    }
}