package ffc.airsync.api.services.analytic

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.resourceFile
import ffc.airsync.api.services.MongoAbsConnect
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.analyze.HealthAnalyzer
import ffc.entity.healthcare.analyze.HealthIssue.Issue
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoAnalyticDAOTest {

    lateinit var client: MongoClient
    lateinit var server: MongoServer

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    private val providerId = "1bbd7f5ebc920637b04c7796"
    private val patientId = "2bbd7f5ebc920637b04c7796"
    private val houseId = "3bbd7f5ebc920637b04c7796"
    lateinit var dao: AnalyticDAO

    val analytic1 = resourceFile("analytic.json").parseTo<HealthAnalyzer>()

    @Before
    fun setUp() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = MongoAnalyticDAO(serverAddress.hostString, serverAddress.port)
    }

    @After
    fun tearDown() {
        client.close()
        server.shutdownNow()
    }

    @Test
    fun insert() {
        with(dao.insert(ORG_ID, patientId, houseId, analytic1)) {
            result.forEach { key, value ->
                value.issue `should equal` analytic1.result[key]!!.issue
            }
        }
    }

    @Test
    fun getByPersonId() {
        dao.insert(ORG_ID, patientId, houseId, analytic1)

        dao.getByPersonId(ORG_ID, patientId).result.forEach { key, value ->
            value.issue `should equal` analytic1.result[key]!!.issue
        }
    }

    @Test
    fun getByHouseId() {
        dao.insert(ORG_ID, patientId, houseId, analytic1)

        dao.getByHouseId(ORG_ID, houseId).first().result.forEach { key, value ->
            value.issue `should equal` analytic1.result[key]!!.issue
        }
    }

    @Test(expected = NoSuchElementException::class)
    fun deleteByPersonId() {
        dao.insert(ORG_ID, patientId, houseId, analytic1)
        dao.deleteByPersonId(ORG_ID, patientId)

        dao.getByPersonId(ORG_ID, patientId)
    }

    @Test(expected = NoSuchElementException::class)
    fun deleteByOrgId() {
        dao.insert(ORG_ID, patientId, houseId, analytic1)
        dao.deleteByOrgId(ORG_ID)

        dao.getByPersonId(ORG_ID, patientId)
    }

    @Test
    fun queryFound() {
        dao.insert(ORG_ID, patientId, houseId, analytic1)

        dao.query(ORG_ID, Issue.DM.toString()).size `should be equal to` 1
        dao.query(ORG_ID, Issue.DEMENTIA.toString()).size `should be equal to` 1
        dao.query(ORG_ID, Issue.DEPRESSIVE.toString()).size `should be equal to` 0
    }
}
