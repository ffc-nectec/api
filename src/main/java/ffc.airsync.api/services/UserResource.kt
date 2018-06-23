package ffc.airsync.api.services

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.UserService
import ffc.entity.User
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.xml.bind.DatatypeConverter


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/org")
class UserResource {


    @RolesAllowed("ORG")
    @POST
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/user")
    fun create(@Context req: HttpServletRequest,
               @PathParam("orgUuid") orgId: String,
               userList: ArrayList<User>): Response {

        printDebug("Raw user list.")
        userList.forEach {
            printDebug("User = " + it.username + " Pass = " + it.password)
        }

        UserService.create(orgId, userList)
        return Response.status(Response.Status.CREATED).build()


    }


    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/authorize")
    fun registerMobile(@Context req: HttpServletRequest,
                       @PathParam("orgId") orgId: String): Response {

        val httpHeader = req.buildHeaderMap()
        val token = httpHeader["Authorization"]?.replaceFirst("Basic ", "")
                ?: throw NotAuthorizedException("Not Authorization")
        val userpass = DatatypeConverter.parseBase64Binary(token).toString(charset("UTF-8")).split(":")
        val user = userpass.get(index = 0)
        val pass = userpass.get(index = 1)


        printDebug("Mobile Login Auid = " + orgId +
                " User = " + user +
                " Pass = " + pass)
        val tokenMessage = UserService.login(orgId, user, pass)


        printDebug("Token is $tokenMessage")
        return Response.status(Response.Status.CREATED).entity(tokenMessage).build()
    }

}
