package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.Lang
import ffc.entity.healthcare.Disease
import ffc.entity.util.generateTempId
import org.amshove.kluent.`should equal`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoDiseaseDaoTest {

    lateinit var dao: DiseaseDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = diseases(serverAddress.hostString, serverAddress.port)

        dao.insert(Disease(generateTempId(), "Fall", "HHXX001Y").apply { translation[Lang.th] = "อ้วนซ้ำซ้อน" })
        dao.insert(
            Disease(
                generateTempId(),
                "Fall2",
                "HHXX002Y",
                isNCD = true,
                isChronic = true,
                isEpimedic = true
            ).apply { translation[Lang.th] = "กินไม่หยุด" })
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insertReturnResult() {
        val result = dao.insert(Disease(generateTempId(), "Fall99", "HHXX099Y"))

        result.name `should equal` "Fall99"
    }

    @Test
    fun insertListAndQuery() {
        val diseaseList = arrayListOf<Disease>().apply {
            add(Disease(generateTempId(), "Fall3", "HHXX003T"))
            add(Disease(generateTempId(), "Fall4", "HHXX004T"))
        }
        dao.insert(diseaseList)

        dao.find("HHXX003T").last().name `should equal` "Fall3"
        dao.find("HHXX004T").last().name `should equal` "Fall4"
    }

    @Test
    fun queryLang() {
        dao.find("HHXX001Y", Lang.th).last().name `should equal` "อ้วนซ้ำซ้อน"
        dao.find("HHXX001Y", Lang.en).last().name `should equal` "Fall"
    }
}
