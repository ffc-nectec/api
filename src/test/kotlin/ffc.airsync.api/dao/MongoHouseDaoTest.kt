package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.House
import ffc.entity.Person
import ffc.entity.ThaiCitizenId
import ffc.entity.ThaiHouseholdId
import ffc.entity.gson.toJson
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Disease
import ffc.entity.update
import ffc.entity.util.generateTempId
import me.piruin.geok.geometry.Point
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoHouseDaoTest {

    val ORG_ID = "87543432abcf432123456785"

    lateinit var dao: HouseDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    lateinit var maxHouse: House
    lateinit var someHouse: House

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = DaoFactory().houses(serverAddress.hostString, serverAddress.port)

        val house = createHouse("12348764532", "999/888")
        println("house obj = ${house.toJson()}")

        maxHouse = dao.insert(ORG_ID, createHouse("12348764532", "999/888"))
        someHouse = dao.insert(ORG_ID, createHouse("11111111111", "888/777"))
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    fun createHouse(identity: String, no: String): House {
        return House().apply {
            this.identity = ThaiHouseholdId(identity)
            this.no = no
            road = "สาธร"
            location = Point(13.123321, 113.7765654)
            people.add(createPerson("1234544323423", "นาย มั่นคง มั่งคั่ง"))
            people.add(createPerson("8909877615243", "นางสาว สวย น่ารัก"))
        }
    }

    private fun createPerson(idCard: String, stringName: String): Person {
        val nameStruct = stringName.split(" ")
        return Person().apply {
            identities.add(ThaiCitizenId(idCard))
            prename = nameStruct[0]
            firstname = nameStruct[1]
            lastname = nameStruct[2]
            sex = if (nameStruct[0].trim() == "นาย") Person.Sex.MALE else Person.Sex.FEMALE
            birthDate = LocalDate.now().minusMonths(240)
            chronics.add(Chronic(Disease(generateTempId(), "fair", "dx001")))
        }
    }

    @Test
    fun findByHouseId() {
        dao.find(maxHouse.id)!!.id `should be equal to` maxHouse.id
        dao.find(someHouse.id)!!.id `should be equal to` someHouse.id
    }

    @Test
    fun updateHouse() {
        val houseFind = dao.find(maxHouse.id)
        houseFind!!.update<House> {
            road = "เชียงราก"
            no = "123/556"
        }
        dao.update(houseFind)
        val houseUpdate = dao.find(maxHouse.id)

        houseUpdate!!.road `should equal` "เชียงราก"
        houseUpdate.no `should equal` "123/556"
    }

    @Test
    fun timestampUpdate() {
        val houseFind = dao.find(maxHouse.id)
        val oldTimestamp = houseFind!!.timestamp
        houseFind.update<House> {
            road = "เชียงราก"
        }
        dao.update(houseFind)
        val houseUpdate = dao.find(maxHouse.id)

        houseFind.timestamp `should not equal` oldTimestamp
        houseFind.timestamp `should equal` houseUpdate!!.timestamp
    }

    @Test
    fun delete() {
        dao.delete(maxHouse.id)

        dao.find(maxHouse.id) `should equal` null
    }

    @Test
    fun removeByOrgId() {
        dao.findAll(ORG_ID).size `should be equal to` 2

        dao.removeByOrgId(ORG_ID)

        dao.findAll(ORG_ID).size `should be equal to` 0
    }
}
