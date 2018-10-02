package ffc.airsync.api.services.notification

import ffc.airsync.api.printDebug
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FirebaseResource {

    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/firebasetoken")
    @RolesAllowed("ORG", "ADMIN")
    fun updateToken(@PathParam("orgId") orgId: String, firebaseToken: HashMap<String, String>): Response {
        printDebug("Call update Firebase Token OrgID $orgId Firebase Token = ${firebaseToken["firebasetoken"]}")

        notification.createFirebase(orgId, firebaseToken["firebasetoken"]!!, true)

        return Response.status(200).build()
    }

    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/mobilefirebasetoken")
    @RolesAllowed("USER", "PROVIDER", "SURVEYOR")
    fun createToken(@PathParam("orgId") orgId: String, firebaseToken: HashMap<String, String>): Response {
        printDebug("Call update Firebase Token by OrgID $orgId Firebase Token = ${firebaseToken["firebasetoken"]}")
        notification.createFirebase(orgId, firebaseToken["firebasetoken"]!!, false)

        return Response.status(200).build()
    }
}
