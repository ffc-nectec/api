package ffc.airsync.api.services.user

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.resourceFile
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.parseTo
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * ทดสอบกับฐานคลองหลวงแล้ว Error พบ user ซ้ำ
 */
class MongoUserIssuerTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private lateinit var nectecOrg: Organization
    private lateinit var dao: MongoUserDao

    val userList2 = resourceFile("user.json").parseTo<List<User>>()

    @Before
    fun initDb() {
        dao = MongoUserDao(mongo.address)
        val org = Org("รพสตNectec", "192.168.99.3")
        nectecOrg = MongoOrgDao(mongo.address).insert(org)
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
