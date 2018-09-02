package ffc.airsync.api.services

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.FirebaseService
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
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
    fun updateToken(@PathParam("orgId") orgId: String, firebaseToken: HashMap<String, String>): Response {

        printDebug("Call update Firebase Token OrgID $orgId Firebase Token = ${firebaseToken["firebasetoken"]}")

        FirebaseService.createOrgToken(orgId, firebaseToken["firebasetoken"]!!)

        return Response.status(200).build()
    }

    @RolesAllowed("USER")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/mobilefirebasetoken")
    fun createToken(@PathParam("orgId") orgId: String, firebaseToken: HashMap<String, String>): Response {

        printDebug("Call update Firebase Token by OrgID $orgId Firebase Token = ${firebaseToken["firebasetoken"]}")
        FirebaseService.createMobileToken(orgId, firebaseToken["firebasetoken"]!!)

        return Response.status(200).build()
    }
}
