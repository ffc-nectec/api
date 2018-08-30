package ffc.airsync.api.dao

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.entity.Token
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoTokenDaoTest {
    private val ORG_ID = "abcdeff"

    lateinit var dao: TokenDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    lateinit var tokenMax: Token
    lateinit var tokenBee: Token

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)

        dao = tokens(serverAddress.hostString, serverAddress.port)

        tokenMax = dao.create(User("Thanachai", User.Role.ORG), ORG_ID)
        tokenBee = dao.create(User("Morakot", User.Role.USER), ORG_ID)
        dao.create(User("Cat", User.Role.USER), "abc1234")
        dao.create(User("Dog", User.Role.USER), "abc1234")
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    fun User(name: String, role: User.Role = User.Role.USER): User =
            User().apply {
                this.name = name
                password = "catbite"
                this.role = role
            }

    @Test
    fun createAndFindToken() {
        val token = dao.find(tokenMax.token)

        token `should not be` null
        token!!.token `should be equal to` tokenMax.token
    }

    @Test
    fun createAndCheckProperty() {
        val token = dao.find(tokenMax.token)

        token `should not be` null
        token!!.user.name `should be equal to` "Thanachai"
        tokenMax.createDate `should equal` token.createDate
        tokenMax.expireDate `should equal` token.expireDate
    }

    @Test
    fun findByOrg() {
        val tokenList = dao.findByOrgId(ORG_ID)

        tokenList.size `should be equal to` 2
        tokenList.find { it.user.name == "Thanachai" } `should not be` null
        tokenList.find { it.user.name == "Morakot" } `should not be` null
        tokenList.find { it.user.name == "Phutipong" } `should equal` null
    }

    @Test
    fun removeToken() {
        dao.remove(tokenMax.token) `should be equal to` true
        dao.find(tokenBee.token) `should not be` null
        dao.find(tokenMax.token)
    }

    @Test
    fun removeTokenByGroupOrgAndFind() {
        dao.removeByOrgId(ORG_ID)

        dao.findByOrgId(ORG_ID).size `should be equal to` 0
    }
}
