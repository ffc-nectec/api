package ffc.airsync.api.services.user

import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.token.tokens
import ffc.entity.Token
import ffc.entity.User
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.ForbiddenException
import javax.ws.rs.GET
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserResource {
    @POST
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/user")
    @RolesAllowed("ORG", "ADMIN")
    fun create(@PathParam("orgUuid") orgId: String, user: List<User>): Response {
        val usersUpdate = user.map { users.insertUser(it, orgId) }
        return Response.status(Response.Status.CREATED).entity(usersUpdate).build()
    }

    @GET
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/user")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun get(@PathParam("orgUuid") orgId: String): Response {
        return Response.status(Response.Status.OK).entity(users.findUser(orgId)).build()
    }

    @POST
    @Path("/$ORGIDTYPE/authorize")
    fun createAuthorizeToken(@PathParam("orgId") orgId: String, body: LoginBody): Token {
        return login(orgId, body.username, body.password)
    }

    @POST
    @Path("/$ORGIDTYPE/authorize/activate")
    fun createAuthorizeActivate(@PathParam("orgId") orgId: String, body: LoginBody): Token {
        return login(orgId, body.username, body.password)
    }

    companion object {
        val logger = getLogger()
    }

    fun login(orgId: String, username: String, pass: String): Token {
        if (UserDao.isBlockUser(username)) throw ForbiddenException("User ไม่มีสิทธิ์ในการใช้งาน")
        val user = users.findThat(orgId, username, pass)
        if (user != null) {
            user.orgId = orgId
            return tokens.create(user, orgId)
        }
        throw NotAuthorizedException("Not Auth")
    }

    class LoginBody(val username: String, val password: String)
    class LoginBodyWithOtp(val username: String, val password: String, val otp: String)
}
