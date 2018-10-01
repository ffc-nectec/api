package ffc.airsync.api.dao

import ffc.airsync.api.services.org.orgs
import ffc.airsync.api.services.user.users
import ffc.entity.Organization
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.junit.Ignore
import org.junit.Test

@Ignore("ทดสอบกับเครื่อง Dev เท่านั้น")
class RealTest {

    lateinit var nectecOrg: Organization

    @Test
    fun insertUser() {
        nectecOrg = orgs.insert(Org("รพ.สต.Nectec", "192.168.99.3"))
        val user = users.insertUser(User("Sommai"), nectecOrg.id)
        println(user.toString())
        user.name `should be equal to` "Sommai"
        orgs.remove(nectecOrg.id)
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
}
