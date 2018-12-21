package ffc.airsync.api.services.village

import ffc.airsync.api.printDebug
import ffc.airsync.api.resorceCall
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.VILLAGETYPE
import ffc.entity.Village
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class VillageResource {
    @POST
    @Path("/$ORGIDTYPE/village")
    @RolesAllowed("ORG", "ADMIN")
    fun create(@PathParam("orgId") orgId: String, village: Village): Village {
        return resorceCall {
            villages.insert(orgId, village)
        }
    }

    @POST
    @Path("/$ORGIDTYPE/villages")
    @RolesAllowed("ORG", "ADMIN")
    fun create(@PathParam("orgId") orgId: String, village: List<Village>): List<Village> {
        printDebug("Create Village")
        val output = resorceCall { villages.insert(orgId, village) }
        printDebug("Finish Create Village")
        return output
    }

    @PUT
    @Path("/$ORGIDTYPE/village/$VILLAGETYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("villageId") villageId: String,
        village: Village
    ): Village {
        require(villageId == village.id) { "ไม่สามารถ update ได้ เนื่องจาก id ไม่ตรงกับเอกสาร" }
        return resorceCall { villages.update(orgId, village) }
    }

    @DELETE
    @Path("/$ORGIDTYPE/village/$VILLAGETYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun delete(
        @PathParam("orgId") orgId: String,
        @PathParam("villageId") villageId: String
    ) {
        return resorceCall { villages.delete(orgId, villageId) }
    }

    @DELETE
    @Path("/$ORGIDTYPE/villages")
    @RolesAllowed("ORG", "ADMIN")
    fun deleteOrg(
        @PathParam("orgId") orgId: String
    ) {
        return resorceCall { villages.removeByOrgId(orgId) }
    }

    @GET
    @Path("/$ORGIDTYPE/village/$VILLAGETYPE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun get(
        @PathParam("orgId") orgId: String,
        @PathParam("villageId") villageId: String
    ): Village {
        return resorceCall { villages.get(orgId, villageId) }
    }

    @GET
    @Path("/$ORGIDTYPE/village")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun find(
        @PathParam("orgId") orgId: String,
        @QueryParam("query") query: String?
    ): List<Village> {
        return if (query != null) {
            resorceCall { villages.find(orgId, query) }
        } else {
            villages.find(orgId)
        }
    }
}
