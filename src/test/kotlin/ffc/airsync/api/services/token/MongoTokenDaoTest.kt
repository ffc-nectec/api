package ffc.airsync.api.services.token

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.Token
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoTokenDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: TokenDao
    lateinit var tokenMax: Token
    lateinit var tokenBee: Token

    @Before
    fun initDb() {

        dao = MongoTokenDao()

        tokenMax = dao.create(User("Thanachai", User.Role.ADMIN), ORG_ID)
        tokenBee = dao.create(User("Morakot", User.Role.PROVIDER), ORG_ID)
        dao.create(User("Cat", User.Role.PROVIDER), "5bbd7f5ebc920637b04c7797")
        dao.create(User("Dog", User.Role.PROVIDER), "5bbd7f5ebc920637b04c7797")
    }

    fun User(name: String, role: User.Role = User.Role.PROVIDER): User =
        User().apply {
            this.name = name
            password = "catbite"
            this.roles.add(role)
        }

    @Test
    fun createAndFindToken() {
        val token = dao.login(tokenMax.token, ORG_ID)

        token `should not be` null
        token!!.token `should be equal to` tokenMax.token
    }

    @Test
    fun createAndCheckProperty() {
        val token = dao.login(tokenMax.token, ORG_ID)

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
        dao.login(tokenBee.token, ORG_ID) `should not be` null
        dao.login(tokenMax.token, ORG_ID) `should equal` null
    }

    @Test
    fun removeTokenByGroupOrgAndFind() {
        dao.removeByOrgId(ORG_ID)

        dao.findByOrgId(ORG_ID).size `should be equal to` 0
    }

    @Test()
    fun loginFail() {
        dao.login(tokenMax.token, "5bbd7f5ebc920637b04c7799") `should equal` null
    }

    @Test
    fun loginFail2() {
        dao.login("jhjhdsjhdfieisfdsdfa", ORG_ID) `should equal` null
    }
}
