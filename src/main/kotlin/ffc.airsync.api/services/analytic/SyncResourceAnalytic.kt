package ffc.airsync.api.services.analytic

import ffc.airsync.api.services.BLOCKTYPE
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.person.persons
import ffc.entity.healthcare.analyze.HealthAnalyzer
import org.bson.types.ObjectId
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SyncResourceAnalytic {

    @POST
    @Path("/$ORGIDTYPE/healthanalyze/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun insertBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        healthAnalyzer: Map<String, HealthAnalyzer>
    ): Map<String, HealthAnalyzer> {
        return analyzers.insertBlock(
            orgId = orgId,
            block = block,
            healthAnalyzer = healthAnalyzer,
            lookupHouse = { persons.findHouseId(orgId, it) }
        )
    }

    @GET
    @Path("/$ORGIDTYPE/healthanalyze/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun getBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Map<String, HealthAnalyzer> {
        return analyzers.getBlock(
            orgId = orgId,
            block = block
        )
    }

    @PUT
    @Path("/$ORGIDTYPE/healthanalyze/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun confirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        analyzers.confirmBlock(
            orgId = orgId,
            block = block
        )
    }

    @DELETE
    @Path("/$ORGIDTYPE/healthanalyze/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun unConfirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {

        analyzers.unConfirmBlock(
            orgId = orgId,
            block = block
        )
    }
}
