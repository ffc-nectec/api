package ffc.airsync.api.services.user

import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Token
import ffc.entity.User
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
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
    fun create(@PathParam("orgUuid") orgId: String, users: List<User>): Response {
        users.forEach {
            it.roles.add(it.role)
        }
        val usersUpdate = UserService.create(orgId, users)
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
        return UserService.login(orgId, body.username, body.password)
    }

    companion object {
        val logger = getLogger()
    }

    class LoginBody(val username: String, val password: String)
}
