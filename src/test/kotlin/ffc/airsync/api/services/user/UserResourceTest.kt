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

package ffc.airsync.api.services.user

import com.nhaarman.mockitokotlin2.eq
import ffc.airsync.api.GsonJerseyProvider
import ffc.airsync.api.filter.RequireError
import ffc.airsync.api.filter.SuccessToCreatedResponse
import ffc.airsync.api.security.otp.OtpDao
import ffc.airsync.api.security.token.TokenDao
import ffc.airsync.api.services.user.activate.ActivateDao
import ffc.entity.Link
import ffc.entity.System
import ffc.entity.Token
import ffc.entity.User
import org.amshove.kluent.When
import org.amshove.kluent.`should equal`
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.mock
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.glassfish.jersey.test.JerseyTest
import org.junit.Test
import javax.ws.rs.core.Application
import javax.ws.rs.core.MediaType

private const val ORG_ID1 = "5bbd7f5ebc920637b04c7796"

class UserResourceTest : JerseyTest() {
    private lateinit var mouckOtpDao: OtpDao
    private lateinit var mouckUserDao: UserDao
    private lateinit var mouckActivateDao: ActivateDao
    private lateinit var mouckTokenDao: TokenDao

    private lateinit var maxUser: User

    override fun configure(): Application {
        mouckUserDao = mock()
        maxUser = createUser("max", "191", ORG_ID1)
        When calling mouckUserDao.findThat(ORG_ID1, "max", "191") itReturns
            maxUser

        mouckActivateDao = mock()
        When calling mouckActivateDao.checkActivate(ORG_ID1, maxUser.id) itReturns
            true

        mouckOtpDao = mock()
        When calling mouckOtpDao.isValid(eq(ORG_ID1), eq("123456"), any()) itReturns true
        When calling mouckOtpDao.isValid(eq(ORG_ID1), eq("654321"), any()) itReturns false

        mouckTokenDao = mock()
        When calling mouckTokenDao.create(maxUser, ORG_ID1) itReturns Token(maxUser, "abcdefghijk")

        return ResourceConfig()
            .registerClasses(RequireError::class.java)
            .register(RolesAllowedDynamicFeature::class.java)
            .register(GsonJerseyProvider::class.java)
            .register(SuccessToCreatedResponse::class.java)
            .register(UserResource(mouckUserDao, mouckOtpDao, mouckActivateDao, mouckTokenDao))
    }

    @Test
    fun userLoginUserPassFail() {
        val loginBody = UserResource.LoginBody("fail", "password")
        val res = target("org/$ORG_ID1/authorize").request().post(
            javax.ws.rs.client.Entity.entity(loginBody, MediaType.APPLICATION_JSON_TYPE)
        )
        res.status `should equal` 401
    }

    @Test
    fun userLoginPass() {
        val loginBody = UserResource.LoginBody("max", "191")
        val res = target("org/$ORG_ID1/authorize").request().post(
            javax.ws.rs.client.Entity.entity(loginBody, MediaType.APPLICATION_JSON_TYPE)
        )
        res.status `should equal` 201
        val token = res.readEntity(Map::class.java)
        token["token"] `should equal` "abcdefghijk"
    }

    @Test
    fun userLoginNoActivate() {
        When calling mouckActivateDao.checkActivate(ORG_ID1, maxUser.id) itReturns
            false

        val loginBody = UserResource.LoginBody("max", "191")
        val res = target("org/$ORG_ID1/authorize").request().post(
            javax.ws.rs.client.Entity.entity(loginBody, MediaType.APPLICATION_JSON_TYPE)
        )
        res.status `should equal` 401
    }

    @Test
    fun activateUser() {
        When calling mouckActivateDao.checkActivate(ORG_ID1, maxUser.id) itReturns
            false
        val loginBodyWithOtp = UserResource.LoginBodyWithOtp("max", "191", "123456")

        val res = target("org/$ORG_ID1/user/activate").request().put(
            javax.ws.rs.client.Entity.entity(loginBodyWithOtp, MediaType.APPLICATION_JSON_TYPE)
        )

        res.status `should equal` 200
        val token = res.readEntity(Map::class.java)
        token["token"] `should equal` "abcdefghijk"
    }

    @Test
    fun activateFailOtp() {
        When calling mouckActivateDao.checkActivate(ORG_ID1, maxUser.id) itReturns
            false
        val loginBodyWithOtp = UserResource.LoginBodyWithOtp("max", "191", "654321")

        val res = target("org/$ORG_ID1/user/activate").request().put(
            javax.ws.rs.client.Entity.entity(loginBodyWithOtp, MediaType.APPLICATION_JSON_TYPE)
        )

        res.status `should equal` 401
    }

    fun createUser(
        name: String,
        pass: String,
        orgId: String,
        roles: MutableList<User.Role> = mutableListOf(User.Role.PROVIDER)
    ): User = User(name = name, password = pass).apply {
        this.roles.addAll(roles)
        this.orgId = orgId
        this.displayName = "${name}คุง"
        this.link = Link(System.JHICS)
    }
}
