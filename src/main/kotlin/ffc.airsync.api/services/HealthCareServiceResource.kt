package ffc.airsync.api.services

import ffc.airsync.api.services.module.HomeVisitService
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

const val PART_HEALTHCARESERVICE = "healthcareservice"

@Path("/org")
class VisitResource {

    @RolesAllowed("USER", "ORG")
    @POST
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/$PART_HEALTHCARESERVICE")
    fun create(
        @PathParam("orgId") orgId: String,
        healthCareService: HealthCareService
    ): Response {

        val respond = when (healthCareService) {
            is HomeVisit -> HomeVisitService.create(healthCareService, orgId)
            else -> null
        }
        return Response.status(201).entity(respond).build()
    }

    @RolesAllowed("USER", "ORG")
    @GET
    @Path("/{orgUuid:([\\dabcdefABCDEF].*)}/$PART_HEALTHCARESERVICE/{visitId:([\\dabcdefABCDEF].*)}")
    fun get(
        @PathParam("orgId") orgId: String,
        @PathParam("visitId") visitId: String
    ): HomeVisit {
        return HomeVisitService.get(orgId, visitId)
    }
}
