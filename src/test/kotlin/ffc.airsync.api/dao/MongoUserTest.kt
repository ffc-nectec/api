package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.toJson
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoUserTest {

    val DATABASE_NAME = "ffcTest"
    val DB_COLLECTION = "user"

    lateinit var nectecOrg: Organization
    lateinit var dao: UserDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        val orgDao = MongoOrgDao(serverAddress.hostString, serverAddress.port, DATABASE_NAME, DB_COLLECTION)

        dao = MongoUserDao(serverAddress.hostString, serverAddress.port, DATABASE_NAME, DB_COLLECTION)

        nectecOrg = orgDao.insert(Org("รพ.สต.Nectec", "192.168.99.3"))
        orgDao.findAll().forEach {
            println("Org = ${it.toJson()}")
        }

        dao.findUser(nectecOrg.id).forEach {
            println("User = ${it.name}")
        }
        dao.findUser(nectecOrg.id)
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
            Organization().apply {
                this.name = name
                users.add(User("maxkung", User.Role.ORG))
                users.add(User("cat"))
                users.add(User("dog"))
                users.add(User("adm"))
                users.add(User("ADM"))
                users.add(User("newuser"))
                users.add(User("usr_db"))
                users.add(User("Drug_Store_Admin"))
                bundle["lastKnownIp"] = ip // "203.111.222.123"
            }

    fun User(name: String, role: User.Role = User.Role.USER): User =
            User().apply {
                this.name = name
                password = "catbite"
                this.role = role
            }

    @Test
    fun findAll() {
        val users = dao.findUser(nectecOrg.id)

        users.find { it.name == "maxkung" } `should not equal` null
    }

    @Test
    fun insertUser() {
        val user = dao.insertUser(User("Sommai"), nectecOrg.id)
        println(user.toString())

        user.name `should be equal to` "Sommai"
    }

    @Test
    fun loginBlockUser() {
        dao.getUser("maxkung", "catbite", nectecOrg.id) `should not equal` null
        dao.getUser("cat", "catbite", nectecOrg.id) `should not equal` null
        dao.getUser("dog", "catbite", nectecOrg.id) `should not equal` null
        dao.getUser("adm", "catbite", nectecOrg.id) `should equal` null
        dao.getUser("ADM", "catbite", nectecOrg.id) `should equal` null
        dao.getUser("newuser", "catbite", nectecOrg.id) `should equal` null
        dao.getUser("usr_db", "catbite", nectecOrg.id) `should equal` null
        dao.getUser("Drug_Store_Admin", "catbite", nectecOrg.id) `should equal` null
    }
}
