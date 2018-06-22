package ffc.airsync.api.services

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.FirebaseService
import ffc.entity.firebase.FirebaseToken
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/org")
class FirebaseResource {

    @RolesAllowed("ORG")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/firebasetoken")
    fun updateToken(@Context req: HttpServletRequest,
                    @PathParam("orgId") orgId: String,
                    firebaseToken: FirebaseToken): Response {

        printDebug("Call update Firebase Token by ip = " + req.remoteAddr + " OrgID $orgId Firebase Token = ${firebaseToken.firebasetoken}")

        val httpHeader = req.buildHeaderMap()
        val token = httpHeader["Authorization"]?.replaceFirst("Bearer ", "")
                ?: throw NotAuthorizedException("Not Authorization")


        FirebaseService.updateToken(UUID.fromString(token), orgId, firebaseToken)

        return Response.status(200).build()
    }


    @RolesAllowed("USER")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/mobilefirebasetoken")
    fun createToken(@Context req: HttpServletRequest,
                    @PathParam("orgId") orgId: String,
                    firebaseToken: FirebaseToken): Response {

        printDebug("Call update Firebase Token by ip = " + req.remoteAddr + " OrgID $orgId Firebase Token = ${firebaseToken.firebasetoken}")

        val httpHeader = req.buildHeaderMap()
        val token = httpHeader["Authorization"]?.replaceFirst("Bearer ", "")
                ?: throw NotAuthorizedException("Not Authorization")


        FirebaseService.updateToken(UUID.fromString(token), orgId, firebaseToken)

        return Response.status(200).build()
    }

}
