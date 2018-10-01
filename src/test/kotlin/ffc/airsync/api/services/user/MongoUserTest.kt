package ffc.airsync.api.services.user

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.dao.MongoAbsConnect
import ffc.airsync.api.dao.insert
import ffc.entity.Organization
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoUserTest {

    private lateinit var nectecOrg: Organization
    private lateinit var dao: MongoUserDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = MongoUserDao(serverAddress.hostString, serverAddress.port)
        val collection = client.getDatabase(dao.dbName).getCollection(dao.collection)
        nectecOrg = collection.insert(Org("รพ.สต.Nectec", "192.168.99.3"))
        users.forEach { dao.insertUser(it, nectecOrg.id) }
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
            Organization().apply {
                this.name = name
                bundle["lastKnownIp"] = ip // "203.111.222.123"
            }

    val users = listOf(
            User("maxkung", User.Role.ORG),
            User("cat"),
            User("dog"),
            User("adm"),
            User("ADM"),
            User("newuser"),
            User("usr_db"),
            User("Drug_Store_Admin")
    )

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
        user.password `should not equal` null
    }

    @Test
    fun login() {
        val user = dao.findThat(nectecOrg.id, "maxkung", "catbite")

        user!!.name `should equal` "maxkung"
    }

    @Test
    fun loginBlockUser() {
        UserDao.isBlockUser("maxkung") `should be equal to` false
        UserDao.isBlockUser("cat") `should be equal to` false
        UserDao.isBlockUser("dog") `should be equal to` false
        UserDao.isBlockUser("adm") `should be equal to` true
        UserDao.isBlockUser("ADM") `should be equal to` true
        UserDao.isBlockUser("newuser") `should be equal to` true
        UserDao.isBlockUser("usr_db") `should be equal to` true
        UserDao.isBlockUser("Drug_Store_Admin") `should be equal to` true
    }
}
