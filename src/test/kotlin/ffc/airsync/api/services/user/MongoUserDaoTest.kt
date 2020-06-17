/*
 * Copyright (c) 2019 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ffc.airsync.api.services.user

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.airsync.api.services.org.OrgDao
import ffc.entity.Organization
import ffc.entity.User
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoUserDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private lateinit var nectecOrg: Organization
    private lateinit var mameOrg: Organization
    private lateinit var dao: MongoUserDao
    private lateinit var orgDao: OrgDao

    @Before
    fun initDb() {
        println("Setup db")
        dao = MongoUserDao()
        orgDao = MongoOrgDao()
        nectecOrg = orgDao.insert(Org("รพสตNectec", "192.168.99.3"))
        mameOrg = orgDao.insert(Org("รพสตบางระจัน", "192.192.99.99"))
    }

    @After
    fun tearDown() {
        orgDao.remove(nectecOrg.id)
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
        Organization().apply {
            this.name = name
            bundle["lastKnownIp"] = ip // "203.111.222.123"
            this.users = userList.toMutableList()
        }

    val userList
        get() = listOf(
            createUser("maxkung", User.Role.ADMIN),
            createUser("somYing"),
            createUser("somChai"),
            createUser("Thanachai"),
            createUser("adm"),
            createUser("ADM"),
            createUser("newuser"),
            createUser("usr_db"),
            createUser("Drug_Store_Admin")
        )

    fun createUser(name: String, role: User.Role = User.Role.PROVIDER): User =
        User().apply {
            this.name = name
            password = "catbite"
            this.roles.add(role)
        }

    @Test
    fun findAll() {
        val users = dao.findUser(nectecOrg.id)

        users.find { it.name == "maxkung" } `should not equal` null
    }

    @Test
    fun insertUser() {
        val user = dao.insert(createUser("Sommai"), nectecOrg.id)

        user.name `should be equal to` "Sommai"
        user.password `should not equal` null
        user.isActivated `should be equal to` false
    }

    @Test
    fun updateUser() {
        val user = dao.insert(createUser("Sommai"), nectecOrg.id)
        val user2 = dao.insert(createUser("somTum"), nectecOrg.id)

        user.isActivated `should be equal to` false
        user.activate()
        user.isActivated `should be equal to` true
        val userUpdate = dao.update(user, nectecOrg.id)

        userUpdate.isActivated `should be equal to` true
        dao.getUserById(nectecOrg.id, user2.id).isActivated `should be equal to` false
    }

    @Test
    fun updatePassword() {
        val name = "Sommai"
        val password = "7499"

        dao.insert(createUser(name), nectecOrg.id)
        val user = dao.updatePassword(nectecOrg.id, name, password)

        dao.findThat(nectecOrg.id, name, "catbite") `should equal` null // Login old password
        dao.findThat(nectecOrg.id, name, password) `should not equal` null // Login new password
        user `should not equal` null
        user.name `should be equal to` name
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateNotHaveUser() {
        val name = "Demo99x"
        val password = "7499"
        dao.updatePassword(nectecOrg.id, name, password)
    }

    @Test
    fun updatePasswordMultiOrganization() {
        val name = "Sommai"
        val password = "7499"
        val name2 = "Thanachai"
        val password2 = "1234"

        dao.insert(createUser(name), nectecOrg.id)
        dao.insert(createUser(name2), mameOrg.id)
        val nectecUser = dao.updatePassword(nectecOrg.id, name, password)
        val mameUser = dao.updatePassword(mameOrg.id, name2, password2)

        dao.findThat(nectecOrg.id, name, "catbite") `should equal` null // Login old password
        dao.findThat(nectecOrg.id, name, password) `should not equal` null // Login new password

        dao.findThat(mameOrg.id, name2, "catbite") `should equal` null // Login old password
        dao.findThat(mameOrg.id, name2, password2) `should not equal` null // Login new password

        nectecUser `should not equal` null
        mameUser `should not equal` null
    }

    @Test
    fun setActivateUser() {
        val name = "somChai"
        val name2 = "Thanachai"

        val nectecUser = dao.findThat(nectecOrg.id, name, "catbite")!!
        val mameUser = dao.findThat(mameOrg.id, name2, "catbite")!!

        nectecUser.isActivated `should be equal to` false
        mameUser.isActivated `should be equal to` false
        nectecUser.activate()
        mameUser.activate()

        dao.update(nectecUser, nectecOrg.id).isActivated `should be equal to` true
        dao.update(mameUser, mameOrg.id).isActivated `should be equal to` true

        dao.findThat(nectecOrg.id, name, "catbite")!!.isActivated `should be equal to` true
        dao.findThat(mameOrg.id, name2, "catbite")!!.isActivated `should be equal to` true
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun insertUserActivateCheckFail() {
        dao.insert(createUser("Sommai").apply {
            activate()
        }, nectecOrg.id)
    }

    @Test
    fun getUserById() {
        val user = dao.insert(createUser("Sommai"), nectecOrg.id)

        dao.getUserById(nectecOrg.id, user.id).name `should be equal to` user.name
    }

    @Test
    fun getUserByName() {
        dao.insert(createUser("Sommai"), nectecOrg.id)

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

    @Test
    fun deleteHeadSingleUser() {
        val user1_1 = dao.insert(createUser("Sommai"), nectecOrg.id)
        val user1_2 = dao.insert(createUser("CatMaLo"), nectecOrg.id)
        val user1_3 = dao.insert(createUser("CMM"), nectecOrg.id)
        val user2_1 = dao.insert(createUser("Thanachai"), mameOrg.id)
        val user2_2 = dao.insert(createUser("Mora"), mameOrg.id)
        val user2_3 = dao.insert(createUser("Male"), mameOrg.id)

        dao.delete(nectecOrg.id, listOf(user1_1.id))

        val checkNectecOrg = dao.findUser(nectecOrg.id)
        checkNectecOrg.find { it.id == user1_1.id }?.id `should equal` null
        checkNectecOrg.find { it.id == user1_2.id }?.id `should equal` user1_2.id
        checkNectecOrg.find { it.id == user1_3.id }?.id `should equal` user1_3.id

        val checkMameOrg = dao.findUser(mameOrg.id)
        checkMameOrg.find { it.id == user2_1.id }?.id `should equal` user2_1.id
        checkMameOrg.find { it.id == user2_2.id }?.id `should equal` user2_2.id
        checkMameOrg.find { it.id == user2_3.id }?.id `should equal` user2_3.id
    }

    @Test
    fun deleteTailSingleUser() {
        val user1_1 = dao.insert(createUser("Sommai"), nectecOrg.id)
        val user1_2 = dao.insert(createUser("CatMaLo"), nectecOrg.id)
        val user1_3 = dao.insert(createUser("CMM"), nectecOrg.id)
        val user2_1 = dao.insert(createUser("Thanachai"), mameOrg.id)
        val user2_2 = dao.insert(createUser("Mora"), mameOrg.id)
        val user2_3 = dao.insert(createUser("Male"), mameOrg.id)

        dao.delete(mameOrg.id, listOf(user2_3.id))
        val checkNectecOrg = dao.findUser(nectecOrg.id)
        checkNectecOrg.find { it.id == user1_1.id }?.id `should equal` user1_1.id
        checkNectecOrg.find { it.id == user1_2.id }?.id `should equal` user1_2.id
        checkNectecOrg.find { it.id == user1_3.id }?.id `should equal` user1_3.id

        val checkMameOrg = dao.findUser(mameOrg.id)
        checkMameOrg.find { it.id == user2_1.id }?.id `should equal` user2_1.id
        checkMameOrg.find { it.id == user2_2.id }?.id `should equal` user2_2.id
        checkMameOrg.find { it.id == user2_3.id }?.id `should equal` null
    }

    @Test
    fun deleteMultiUser() {
        val user1_1 = dao.insert(createUser("Sommai"), nectecOrg.id)
        val user1_2 = dao.insert(createUser("CatMaLo"), nectecOrg.id)
        val user1_3 = dao.insert(createUser("CMM"), nectecOrg.id)
        val user2_1 = dao.insert(createUser("Thanachai"), mameOrg.id)
        val user2_2 = dao.insert(createUser("Mora"), mameOrg.id)
        val user2_3 = dao.insert(createUser("Male"), mameOrg.id)

        dao.delete(nectecOrg.id, listOf(user1_2.id, user1_3.id))
        runBlocking {
            val checkNectecOrg = dao.findUser(nectecOrg.id)
            checkNectecOrg.find { it.id == user1_1.id }?.id `should equal` user1_1.id
            checkNectecOrg.find { it.id == user1_2.id }?.id `should equal` null
            checkNectecOrg.find { it.id == user1_3.id }?.id `should equal` null

            val checkMameOrg = dao.findUser(mameOrg.id)
            checkMameOrg.find { it.id == user2_1.id }?.id `should equal` user2_1.id
            checkMameOrg.find { it.id == user2_2.id }?.id `should equal` user2_2.id
            checkMameOrg.find { it.id == user2_3.id }?.id `should equal` user2_3.id
        }

        dao.delete(mameOrg.id, listOf(user2_1.id, user2_2.id))
        runBlocking {
            val checkNectecOrg = dao.findUser(nectecOrg.id)
            checkNectecOrg.find { it.id == user1_1.id }?.id `should equal` user1_1.id
            checkNectecOrg.find { it.id == user1_2.id }?.id `should equal` null
            checkNectecOrg.find { it.id == user1_3.id }?.id `should equal` null

            val checkMameOrg = dao.findUser(mameOrg.id)
            checkMameOrg.find { it.id == user2_1.id }?.id `should equal` null
            checkMameOrg.find { it.id == user2_2.id }?.id `should equal` null
            checkMameOrg.find { it.id == user2_3.id }?.id `should equal` user2_3.id
        }

        dao.delete(nectecOrg.id, listOf(user1_1.id))
        dao.delete(mameOrg.id, listOf(user2_3.id))
        runBlocking {
            val checkNectecOrg = dao.findUser(nectecOrg.id)
            checkNectecOrg.find { it.id == user1_1.id }?.id `should equal` null
            checkNectecOrg.find { it.id == user1_2.id }?.id `should equal` null
            checkNectecOrg.find { it.id == user1_3.id }?.id `should equal` null

            val checkMameOrg = dao.findUser(mameOrg.id)
            checkMameOrg.find { it.id == user2_1.id }?.id `should equal` null
            checkMameOrg.find { it.id == user2_2.id }?.id `should equal` null
            checkMameOrg.find { it.id == user2_3.id }?.id `should equal` null
        }
    }
}
