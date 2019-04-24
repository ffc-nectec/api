package ffc.airsync.api.services.org

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.resourceFile
import ffc.entity.Link
import ffc.entity.Organization
import ffc.entity.System
import ffc.entity.User
import ffc.entity.gson.parseTo
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoOrgDaoTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    lateinit var dao: OrgDao
    lateinit var hahahaOrg: Organization
    lateinit var nectecOrg: Organization

    @Before
    fun initDb() {
        dao = MongoOrgDao()

        hahahaOrg = dao.insert(Org("รพสตHAHAHA", "203.111.222.123").apply {
            displayName = "รพ.สต.HAHAHA"
            tel = "02-388-5555"
            address = "166 ม.99 ต.เนคเทค อ.อยู่ดี จ.กินดี"
            link!!.keys["pcucode"] = "100145"
        })
        nectecOrg = dao.insert(Org("รพสตNectec", "192.168.99.3").apply {
            displayName = "รพ.สต.Nectec"
            tel = "037-261-044"
            address = "161 ม.29 ต.สง่างาม อ.สดใส จ.ผิวผ่อง"
            link!!.keys["pcucode"] = "203"
        })
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
        Organization().apply {
            this.name = name
            users.add(User("maxkung", User.Role.ADMIN))
            users.add(User("somYing"))
            bundle["lastKnownIp"] = ip // "203.111.222.123"
            link = Link(System.JHICS)
        }

    fun User(name: String, role: User.Role = User.Role.PATIENT): User =
        User().apply {
            this.name = name
            password = "catbite"
            roles.add(role)
        }

    @Test
    fun insert() {
        val returnedOrg = dao.insert(Org("รพสตAAA"))

        with(returnedOrg) {
            name `should be equal to` "รพสตAAA"
            isTempId `should be` false
            bundle["lastKnownIp"] = "192.168.99.3"
            users.forEach {
                it.orgId `should equal` this.id
            }
        }
    }

    @Test
    fun insertFromJson() {
        val org = resourceFile("organization.json").parseTo<Organization>()
        val returnOrg = dao.insert(org)

        with(returnOrg) {
            print(id)
            name `should be equal to` org.name
            link `should equal` org.link
            isTempId `should be` false
        }
    }

    @Test
    fun findAll() {
        val orgs = dao.findAll()

        with(orgs) {
            size `should be equal to` 2
            find { it.name == nectecOrg.name } `should not equal` null
            find { it.name == hahahaOrg.name } `should not equal` null
        }
    }

    @Test
    fun findNectecByIp() {
        val orgs = dao.findByIpAddress("192.168.99.3")

        orgs[0].name `should be equal to` nectecOrg.name
    }

    @Test
    fun findHahahaByIp() {
        val orgs = dao.findByIpAddress("203.111.222.123")

        orgs[0].name `should be equal to` hahahaOrg.name
    }

    @Test
    fun findByOrgId() {
        dao.findById(nectecOrg.id) `should equal` nectecOrg
        dao.findById(hahahaOrg.id) `should equal` hahahaOrg
    }

    @Test
    fun findByName() {
        val result = dao.find("Nectec")

        result.count() `should be equal to` 1
        result.first().name `should be equal to` "รพสตNectec"
    }

    @Test
    fun findByTel() {
        val result = dao.find("037-261-044")

        result.count() `should be equal to` 1
        result.first().name `should be equal to` "รพสตNectec"
    }

    @Test
    fun findByAddress() {
        val result = dao.find("สง่างาม")

        result.count() `should be equal to` 1
        result.first().name `should be equal to` "รพสตNectec"
    }

    @Test
    fun findByOfficeId() {
        val result = dao.find("100145")

        result.count() `should be equal to` 1
        result.first().name `should be equal to` "รพสตHAHAHA"
    }

    @Test
    fun removeIsAffect() {
        dao.remove(nectecOrg.id)

        dao.findAll().size `should be equal to` 1
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun nameWrong() {
        dao.insert(Organization().apply {
            name = "*รพ.สต.สง่างง*"
            displayName = "สง่างง"
        })
    }

    @Test
    fun findNameCase1() {
        dao.insert(Organization().apply {
            name = "รพ1-สตสง่างง"
            displayName = "สง่างง"
            users.add(User("maxkung", User.Role.ADMIN))
            users.add(User("somYing"))
        })

        dao.find("รพ.1.").first().name `should be equal to` "รพ1-สตสง่างง"
    }

    @Test
    fun findNameCase2() {
        dao.insert(Organization().apply {
            name = "รพสต-พรทิพา1"
            displayName = "รพ.สต.พรทิพา สาขา 1"
            users.add(User("maxkung", User.Role.ADMIN))
            users.add(User("somYing"))
        })

        dao.find("รพสต-พรทิพา1").first().displayName `should equal` "รพ.สต.พรทิพา สาขา 1"
    }

    @Test
    fun findNameCase3() {
        dao.insert(Organization().apply {
            name = "รพสต-พรทิพา1"
            displayName = "รพ.สต. พรทิพา 1"
            users.add(User("maxkung", User.Role.ADMIN))
            users.add(User("somYing"))
        })

        dao.find("รพ.สต.พรทิพา1").first().displayName `should equal` "รพ.สต. พรทิพา 1"
    }

    @Test
    fun testBug() {

        val maeOrg = dao.insert(Org("โรงพยาบาลส่งเสริมสุขภาพตำบลแม่ข้าวต้ม", "192.168.99.3").apply {
            displayName = "รพ.สต.โรงพยาบาลส่งเสริมสุขภาพตำบลแม่ข้าวต้ม"
            tel = "037-261-044"
            address = "161 ม.29 ต.สง่างาม อ.สดใส จ.ผิวผ่อง"
            link!!.keys["pcucode"] = "203"
        })
        maeOrg.name `should be equal to` "โรงพยาบาลส่งเสริมสุขภาพตำบลแม่ข้าวต้ม"
    }
}
