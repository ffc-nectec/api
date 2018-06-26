package ffc.airsync.api.services

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.FirebaseService
import ffc.entity.firebase.FirebaseToken
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
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

        FirebaseService.createOrgToken(orgId, firebaseToken)

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
        FirebaseService.createMobileToken(orgId, firebaseToken)

        return Response.status(200).build()
    }

}
