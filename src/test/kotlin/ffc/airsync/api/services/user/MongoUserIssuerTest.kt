package ffc.airsync.api.services.user

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.resourceFile
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.parseTo
import org.amshove.kluent.`should be equal to`
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ทดสอบกับฐานคลองหลวงแล้ว Error พบ user ซ้ำ
 */
class MongoUserIssuerTest {

    private lateinit var nectecOrg: Organization
    private lateinit var dao: MongoUserDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    val userList2 = resourceFile("user.json").parseTo<List<User>>()

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = MongoUserDao(serverAddress.hostString, serverAddress.port)
        val org = Org("รพสตNectec", "192.168.99.3")
        nectecOrg = MongoOrgDao(serverAddress.hostString, serverAddress.port).insert(org)
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
            users = listOf(User("maxkung", User.Role.ADMIN)).toMutableList()
        }

    fun User(name: String, role: User.Role = User.Role.USER): User =
        User().apply {
            this.name = name
            password = "catbite"
            this.roles.add(role)
        }

    @Test
    fun insertListUser() {
        val result = userList2.map { dao.insertUser(it, nectecOrg.id) }

        result.size `should be equal to` 2
    }
}
