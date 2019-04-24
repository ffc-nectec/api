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
 */

package ffc.airsync.api.security.token

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

        tokenMax = dao.create(User("Thanachai", User.Role.ORG), ORG_ID)
        tokenBee = dao.create(User("Morakot", User.Role.USER), ORG_ID)
        dao.create(User("Cat", User.Role.USER), "5bbd7f5ebc920637b04c7797")
        dao.create(User("Dog", User.Role.USER), "5bbd7f5ebc920637b04c7797")
    }

    fun User(name: String, role: User.Role = User.Role.USER): User =
        User().apply {
            this.name = name
            password = "catbite"
            this.role = role
        }

    @Test
    fun createAndFindToken() {
        val token = dao.token(tokenMax.token, ORG_ID)

        token `should not be` null
        token!!.token `should be equal to` tokenMax.token
    }

    @Test
    fun createAndCheckProperty() {
        val token = dao.token(tokenMax.token, ORG_ID)

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
        dao.token(tokenBee.token, ORG_ID) `should not be` null
        dao.token(tokenMax.token, ORG_ID) `should equal` null
    }

    @Test
    fun removeTokenByGroupOrgAndFind() {
        dao.removeByOrgId(ORG_ID)

        dao.findByOrgId(ORG_ID).size `should be equal to` 0
    }

    @Test()
    fun loginFail() {
        dao.token(tokenMax.token, "5bbd7f5ebc920637b04c7799") `should equal` null
    }

    @Test
    fun loginFail2() {
        dao.token("jhjhdsjhdfieisfdsdfa", ORG_ID) `should equal` null
    }
}
