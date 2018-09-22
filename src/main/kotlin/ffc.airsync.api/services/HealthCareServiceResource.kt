package ffc.airsync.api.services

import ffc.airsync.api.services.filter.Cache
import ffc.airsync.api.services.module.HomeVisitService
import ffc.entity.User
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

const val PART_HEALTHCARESERVICE = "healthcareservice"

@Path("/org")
class VisitResource {
    @Context
    private var context: SecurityContext? = null

    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HEALTHCARESERVICE")
    fun create(
        @PathParam("orgId") orgId: String,
        healthCareService: HealthCareService
    ): Response {
        roleMapIsSync(healthCareService)
        val respond = when (healthCareService) {
            is HomeVisit -> HomeVisitService.create(healthCareService, orgId)
            else -> null
        }
        return Response.status(201).entity(respond).build()
    }

    @Cache(maxAge = 2)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HEALTHCARESERVICE/{visitId:([\\dabcdefABCDEF].*)}")
    fun get(
        @PathParam("orgId") orgId: String,
        @PathParam("visitId") visitId: String
    ): HomeVisit {
        return HomeVisitService.get(orgId, visitId)
    }

    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    @PUT
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HEALTHCARESERVICE/{visitId:([\\dabcdefABCDEF].*)}")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("visitId") visitId: String,
        healthCareService: HealthCareService
    ): HealthCareService {
        roleMapIsSync(healthCareService)
        return HomeVisitService.update(healthCareService, orgId)
    }

    private fun roleMapIsSync(healthCareService: HealthCareService) {
        when (getTokenRole(context!!)) {
            User.Role.ORG -> healthCareService.link!!.isSynced = true
            else -> healthCareService.link!!.isSynced = false
        }
    }
}
