package ffc.airsync.api.services.notification

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.entity.Link
import ffc.entity.Organization
import ffc.entity.System
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.Before
import org.junit.Test

class MongoFirebaseNotificationTokenDaoTest {

    lateinit var client: MongoClient
    lateinit var server: MongoServer

    lateinit var dao: MongoFirebaseNotificationTokenDao

    lateinit var hahahaOrg: Organization
    lateinit var nectecOrg: Organization

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = MongoFirebaseNotificationTokenDao(serverAddress.hostString, serverAddress.port)

        val orgDao = MongoOrgDao(serverAddress.hostString, serverAddress.port)
        hahahaOrg = orgDao.insert(Org("รพสตHAHAHA", "203.111.222.123").apply {
            tel = "02-388-5555"
            address = "166 ม.99 ต.เนคเทค อ.อยู่ดี จ.กินดี"
            link!!.keys["pcucode"] = "100145"
        })
        nectecOrg = orgDao.insert(Org("รพสตNectec", "192.168.99.3").apply {
            tel = "037-261-044"
            address = "161 ม.29 ต.สง่างาม อ.สดใส จ.ผิวผ่อง"
            link!!.keys["pcucode"] = "203"
        })
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01") = Organization().apply {
        this.name = name
        users.add(User(name = "hello").apply { role = User.Role.ORG })
        bundle["lastKnownIp"] = ip // "203.111.222.123"
        link = Link(System.JHICS)
    }

    fun User(name: String, role: User.Role = User.Role.USER): User = User().apply {
        this.name = name
        password = "catbite"
        this.role = role
    }

    @Test
    fun createAndGetFirebase() {
        dao.createFirebase(nectecOrg.id, "abcdef001", true)
        dao.createFirebase(nectecOrg.id, "abcdef007", false)
        dao.createFirebase(hahahaOrg.id, "abcdef002", true)
        dao.createFirebase(hahahaOrg.id, "abcdef003", false)

        with(dao.getFirebaseToken(nectecOrg.id)) {
            find { it == "abcdef001" } `should not equal` null
            find { it == "abcdef007" } `should not equal` null
            size `should be equal to` 2
        }
        with(dao.getFirebaseToken(hahahaOrg.id)) {
            find { it == "abcdef002" } `should not equal` null
            find { it == "abcdef003" } `should not equal` null
            size `should be equal to` 2
        }
    }

    @Test
    fun removeFirebase() {
        dao.createFirebase(nectecOrg.id, "abcdef001", true)
        dao.createFirebase(nectecOrg.id, "abcdef007", false)
        dao.createFirebase(hahahaOrg.id, "abcdef002", true)
        dao.createFirebase(hahahaOrg.id, "abcdef003", false)

        dao.removeFirebase(nectecOrg.id, "abcdef001", true)
        with(dao.getFirebaseToken(nectecOrg.id)) {
            find { it == "abcdef001" } `should equal` null
        }

        dao.removeFirebase(hahahaOrg.id, "abcdef003", false)
        with(dao.getFirebaseToken(hahahaOrg.id)) {
            forEach { println(it) }
            find { it == "abcdef003" } `should equal` null
        }
    }
}
