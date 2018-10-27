package ffc.airsync.api.services.personrelationsship

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.person.MongoPersonDao
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Disease
import ffc.entity.update
import ffc.entity.util.generateTempId
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should start with`
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoRelationsShipDaoTest {
    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: GenoGramDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer
    lateinit var dog: Person
    lateinit var cat: Person
    lateinit var rabbit: Person

    @Before
    fun initDb() {
        println("Init Database")
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress),
            MongoClientOptions.Builder()
                .maxConnectionIdleTime(5000)
                .connectionsPerHost(5)
                .build())
        MongoAbsConnect.setClient(client)

        val daoPerson = MongoPersonDao(serverAddress.hostString, serverAddress.port)
        dao = MongoRelationsShipDao(serverAddress.hostString, serverAddress.port)
        val misterDog = Person().apply {
            identities.add(ThaiCitizenId("1231233123421"))
            prename = "นาย"
            firstname = "สมชาย"
            lastname = "โคตรกระบือ"
            sex = Person.Sex.MALE
            birthDate = LocalDate.now().minusYears(20)
            chronics.add(Chronic(Disease(generateTempId(), "fair", "dxabc00x")))
            chronics.add(Chronic(Disease(generateTempId(), "fair", "abcffe982")))
            link = Link(System.JHICS)
            link!!.isSynced = false
            houseId = "12345678901"
        }
        val missCat = Person().apply {
            identities.add(ThaiCitizenId("2123455687675"))
            prename = "นางสาว"
            firstname = "สมหญิง"
            lastname = "สมบูรณ์จิต"
            sex = Person.Sex.FEMALE
            birthDate = LocalDate.now().minusYears(27)
            chronics.add(Chronic(Disease(generateTempId(), "floor", "I10")))
            chronics.add(Chronic(Disease(generateTempId(), "fary", "I11")))
            link = Link(System.JHICS)
            link!!.isSynced = true
            houseId = "11111111111"
        }
        val missRabbit = Person().apply {
            identities.add(ThaiCitizenId("1122399087432"))
            prename = "นางสาว"
            firstname = "กระต่าย"
            lastname = "สุดน่ารัก"
            sex = Person.Sex.FEMALE
            birthDate = LocalDate.now().minusYears(22)
            chronics.add(Chronic(Disease(generateTempId(), "sleep", "I10")))
            chronics.add(Chronic(Disease(generateTempId(), "god", "I11")))
            link = Link(System.JHICS)
            link!!.isSynced = false
            houseId = "99887744998"
        }

        dog = daoPerson.insert(ORG_ID, misterDog)
        cat = daoPerson.insert(ORG_ID, missCat)
        rabbit = daoPerson.insert(ORG_ID, missRabbit)

        dog.relationships.add(Person.Relationship(Person.Relate.Child, cat))
        cat.relationships.add(Person.Relationship(Person.Relate.Father, dog))
        daoPerson.update(ORG_ID, dog)
        daoPerson.update(ORG_ID, cat)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
        println("Shutdown Db")
    }

    @Test
    fun get() {
        val dogRelation = dao.get(ORG_ID, dog.id).first()

        dogRelation.id `should be equal to` cat.id
        dogRelation.relate `should equal` Person.Relate.Child
    }

    @Test
    fun update() {
        dog.update {
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
        }
        val dogUpdate = dao.update(ORG_ID, dog.id, dog.relationships)

        rabbit.update {
            relationships.add(Person.Relationship(Person.Relate.Father, dog))
        }
        val rabbitUpdate = dao.update(ORG_ID, rabbit.id, rabbit.relationships)

        dogUpdate.first().id `should be equal to` cat.id
        dogUpdate.last().id `should be equal to` rabbit.id

        rabbitUpdate.first().id `should be equal to` dog.id
        rabbitUpdate.last().id `should be equal to` dog.id
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun validateDuplicateRelation() {
        dog.update {
            relationships.add(Person.Relationship(Person.Relate.Mother, rabbit))
            relationships.add(Person.Relationship(Person.Relate.Mother, rabbit))
        }
        try {
            dao.update(ORG_ID, dog.id, dog.relationships)
        } catch (ex: Exception) {
            ex.message!! `should start with` "พบการใส่ความสัมพันธ์ซ้ำ"
            throw ex
        }
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun validateMotherChild() {
        dog.update {
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
            relationships.add(Person.Relationship(Person.Relate.Mother, rabbit))
        }
        try {
            dao.update(ORG_ID, dog.id, dog.relationships)
        } catch (ex: Exception) {
            ex.message!! `should start with` "ตรวจพบความสัมพันธ์ในครอบครัวแปลก"
            throw ex
        }
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun validateFatherChild() {
        dog.update {
            relationships.add(Person.Relationship(Person.Relate.Father, rabbit))
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
        }
        try {
            dao.update(ORG_ID, dog.id, dog.relationships)
        } catch (ex: Exception) {
            ex.message!! `should start with` "ตรวจพบความสัมพันธ์ในครอบครัวแปลก"
            throw ex
        }
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun childMarriedFather() {
        dog.update {
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
            relationships.add(Person.Relationship(Person.Relate.Married, rabbit))
        }
        try {
            dao.update(ORG_ID, dog.id, dog.relationships)
        } catch (ex: Exception) {
            ex.message!! `should start with` "ตรวจพบความสัมพันธ์ในครอบครัวแปลก"
            throw ex
        }
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun fatherMarriedChild() {
        dog.update {
            relationships.add(Person.Relationship(Person.Relate.Married, rabbit))
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
        }
        try {
            dao.update(ORG_ID, dog.id, dog.relationships)
        } catch (ex: Exception) {
            ex.message!! `should start with` "ตรวจพบความสัมพันธ์ในครอบครัวแปลก"
            throw ex
        }
    }
}
