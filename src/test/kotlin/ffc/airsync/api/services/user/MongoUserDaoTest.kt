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
    private lateinit var dao: MongoUserDao
    private lateinit var orgDao: OrgDao

    @Before
    fun initDb() {
        println("Setup db")
        dao = MongoUserDao()
        val org = Org("รพสตNectec", "192.168.99.3")
        orgDao = MongoOrgDao()
        nectecOrg = orgDao.insert(org)
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

    val userList = listOf(
        createUser("maxkung", User.Role.ADMIN),
        createUser("somYing"),
        createUser("somChai"),
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
        val user = dao.insertUser(createUser("Sommai"), nectecOrg.id)

        user.name `should be equal to` "Sommai"
        user.password `should not equal` null
        user.isActivated `should be equal to` false
    }

    @Test
    fun updateUser() {
        val user = dao.insertUser(createUser("Sommai"), nectecOrg.id)
        val user2 = dao.insertUser(createUser("somTum"), nectecOrg.id)

        user.isActivated `should be equal to` false
        user.activate()
        user.isActivated `should be equal to` true
        val userUpdate = dao.updateUser(user, nectecOrg.id)

        userUpdate.isActivated `should be equal to` true
        dao.getUserById(nectecOrg.id, user2.id).isActivated `should be equal to` false
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun insertUserActivateCheckFail() {
        dao.insertUser(createUser("Sommai").apply {
            activate()
        }, nectecOrg.id)
    }

    @Test
    fun getUserById() {
        val user = dao.insertUser(createUser("Sommai"), nectecOrg.id)

        dao.getUserById(nectecOrg.id, user.id).name `should be equal to` user.name
    }

    @Test
    fun getUserByName() {
        dao.insertUser(createUser("Sommai"), nectecOrg.id)

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
