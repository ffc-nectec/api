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

import ffc.airsync.api.DummyChallenge
import ffc.airsync.api.getLogger
import ffc.airsync.api.security.otp.OrgTimebaseOtp
import ffc.airsync.api.security.token.TokenDao
import ffc.airsync.api.security.token.tokens
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Token
import ffc.entity.User
import javax.annotation.security.RolesAllowed
import javax.ws.rs.BadRequestException
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserResource(
    val usersDao: UserDao = users,
    val tokenDao: TokenDao = tokens
) {
    internal var otpVerify: (orgId: String, otp: String) -> Boolean =
        { orgId, otp -> OrgTimebaseOtp(orgId).verify(otp) }

    @POST
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/user")
    @RolesAllowed("ADMIN")
    fun create(@PathParam("orgUuid") orgId: String, user: List<User>): Response {
        val usersUpdate = user.map { usersDao.insertUser(it, orgId) }
        return Response.status(Response.Status.CREATED).entity(usersUpdate).build()
    }

    @GET
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/user")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun getUsersIn(@PathParam("orgUuid") orgId: String): Response {
        return Response.status(Response.Status.OK).entity(usersDao.findUser(orgId)).build()
    }

    @POST
    @Path("/$ORGIDTYPE/authorize")
    fun createAuthorizeToken(@PathParam("orgId") orgId: String, body: LoginBody): Token {
        val user = getUser(body.username, orgId, body.password)
            ?: throw NotAuthorizedException("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", DummyChallenge())

        if (!user.isActivated) throw NotActivateUserException()

        return tokenDao.create(user, orgId)
    }

    @PUT
    @Path("/$ORGIDTYPE/user/activate")
    fun activateUser(@PathParam("orgId") orgId: String, body: LoginBodyWithOtp): Token {
        if (!otpVerify(orgId, body.otp))
            throw NotAuthorizedException("รหัส OTP ไม่ถูกต้อง โปรดกรอกใหม่", DummyChallenge())
        val user = getUser(body.username, orgId, body.password)
            ?: throw NotAuthorizedException("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", DummyChallenge())

        try {
            user.activate()
            usersDao.updateUser(user, orgId)
        } catch (ex: IllegalStateException) {
            throw BadRequestException("${user.name} ได้รับการ Activate แล้ว")
        }
        return tokenDao.create(user, orgId)
    }

    companion object {
        val logger = getLogger()
    }

    private fun getUser(username: String, orgId: String, pass: String): User? {
        if (UserDao.isBlockUser(username))
            throw BlacklistUserException()

        return usersDao.findThat(orgId, username, pass)
    }

    class LoginBody(val username: String, val password: String)
    class LoginBodyWithOtp(val username: String, val password: String, val otp: String)
}
