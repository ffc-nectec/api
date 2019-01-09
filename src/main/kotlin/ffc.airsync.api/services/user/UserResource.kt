package ffc.airsync.api.services.user

import ffc.airsync.api.filter.Developer
import ffc.airsync.api.printDebug
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.util.getHeaderMap
import ffc.entity.User
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.xml.bind.DatatypeConverter

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Developer
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
    @Developer
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/user")
    @RolesAllowed("ORG", "ADMIN")
    fun get(@PathParam("orgUuid") orgId: String): Response {
        return Response.status(Response.Status.CREATED).entity(users.findUser(orgId)).build()
    }

    @POST
    @Developer
    @Path("/$ORGIDTYPE/authorize")
    fun registerMobile(@Context req: HttpServletRequest, @PathParam("orgId") orgId: String): Response {
        val httpHeader = req.getHeaderMap()
        val token = httpHeader["Authorization"]?.replaceFirst("Basic ", "")
            ?: throw NotAuthorizedException("Not Authorization")
        val userpass = DatatypeConverter.parseBase64Binary(token).toString(charset("UTF-8")).split(":")
        val user = userpass.get(index = 0)
        val pass = userpass.get(index = 1)

        printDebug("Mobile Login Auid = " + orgId + " User = " + user + " Pass = " + pass)
        val tokenMessage = UserService.login(orgId, user, pass)

        printDebug("Token is $tokenMessage")
        return Response.status(Response.Status.CREATED)
            .entity(tokenMessage)
            .header("Authorization", "Bearer ${tokenMessage.token}")
            .build()
    }
}
