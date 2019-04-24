package ffc.airsync.api.services.user

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.entity.Organization
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoUserTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private lateinit var nectecOrg: Organization
    private lateinit var dao: MongoUserDao

    @Before
    fun initDb() {
        dao = MongoUserDao()
        val org = Org("รพสตNectec", "192.168.99.3")
        nectecOrg = MongoOrgDao().insert(org)
    }

    companion object {
        fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
            Organization().apply {
                this.name = name
                bundle["lastKnownIp"] = ip // "203.111.222.123"
                this.users = userList.toMutableList()
            }

        val userList = listOf(
            User("maxkung", User.Role.ADMIN),
            User("somYing"),
            User("somChai"),
            User("adm"),
            User("ADM"),
            User("newuser"),
            User("usr_db"),
            User("Drug_Store_Admin")
        )

        fun User(name: String, role: User.Role = User.Role.PROVIDER): User =
            User().apply {
                this.name = name
                password = "catbite"
                this.roles.add(role)
            }
    }

    @Test
    fun findAll() {
        val users = dao.findUser(nectecOrg.id)

        users.find { it.name == "maxkung" } `should not equal` null
    }

    @Test
    fun insertUser() {
        val user = dao.insertUser(User("Sommai"), nectecOrg.id)

        user.name `should be equal to` "Sommai"
        user.password `should not equal` null
    }

    @Test
    fun getUserById() {
        val user = dao.insertUser(User("Sommai"), nectecOrg.id)

        dao.getUserById(nectecOrg.id, user.id).name `should be equal to` user.name
    }

    @Test
    fun getUserByName() {
        dao.insertUser(User("Sommai"), nectecOrg.id)

        dao.getUserByName(nectecOrg.id, "Sommai")!!.name `should be equal to` "Sommai"
    }

    @Test
    fun login() {
        val user = dao.findThat(nectecOrg.id, "maxkung", "catbite")

        user!!.name `should equal` "maxkung"
    }

    @Test
    fun loginBlockUser() {
        UserDao.isBlockUser("maxkung") `should be equal to` false
        UserDao.isBlockUser("somYing") `should be equal to` false
        UserDao.isBlockUser("somChai") `should be equal to` false
        UserDao.isBlockUser("adm") `should be equal to` true
        UserDao.isBlockUser("ADM") `should be equal to` true
        UserDao.isBlockUser("newuser") `should be equal to` true
        UserDao.isBlockUser("usr_db") `should be equal to` true
        UserDao.isBlockUser("Drug_Store_Admin") `should be equal to` true
    }
}
