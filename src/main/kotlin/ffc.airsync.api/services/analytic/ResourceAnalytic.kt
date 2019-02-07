package ffc.airsync.api.services.analytic

import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.entity.healthcare.analyze.HealthAnalyzer
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
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
class ResourceAnalytic {

    @POST
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/healthanalyze")
    @RolesAllowed("ORG", "ADMIN")
    fun insert(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String,
        healthAnalyzer: HealthAnalyzer
    ): HealthAnalyzer {
        return analyzers.insert(
            orgId = orgId,
            personId = personId,
            healthAnalyzer = healthAnalyzer
        )
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/healthanalyze")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun getByPersonId(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String
    ): HealthAnalyzer {
        return analyzers.getByPersonId(orgId, personId)
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/healthanalyze")
    @RolesAllowed("ORG", "ADMIN")
    fun delete(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String
    ): Response {
        analyzers.deleteByPersonId(orgId, personId)
        return Response.status(Response.Status.OK).build()
    }

    @DELETE
    @Path("/$ORGIDTYPE/healthanalyze")
    @RolesAllowed("ORG", "ADMIN")
    fun deleteOrg(
        @PathParam("orgId") orgId: String
    ): Response {
        analyzers.removeByOrgId(orgId)
        return Response.status(Response.Status.OK).build()
    }
}
