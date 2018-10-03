package ffc.airsync.api.services.disease

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
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
        dao = MongoDiseaseDao(serverAddress.hostString, serverAddress.port)

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
        val result = dao.insert(Disease(generateTempId(), "Fall99", "HHXX099Y"))

        result.name `should equal` "Fall99"
    }

    @Test
    fun insertListAndQuery() {
        val diseaseList = listOf<Disease>(
            Disease(generateTempId(), "Fall3", "HHXX003T"),
            Disease(generateTempId(), "Fall4", "HHXX004T")
        )
        dao.insert(diseaseList)

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
