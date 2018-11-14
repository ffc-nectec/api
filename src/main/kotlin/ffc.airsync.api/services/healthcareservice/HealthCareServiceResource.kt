package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.airsync.api.services.VISITIDTYPE
import ffc.airsync.api.services.notification.broadcastMessage
import ffc.airsync.api.services.notification.notification
import ffc.airsync.api.services.util.getLoginRole
import ffc.entity.User
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import javax.annotation.security.RolesAllowed
import javax.ws.rs.DELETE
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
class HealthCareServiceResource {

    @Context
    private var context: SecurityContext? = null

    @POST
    @Path("/$ORGIDTYPE/$PART_HEALTHCARESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    fun create(
        @PathParam("orgId") orgId: String,
        healthCareService: HealthCareService
    ): Response {
        roleMapIsSync(healthCareService)

        val respond = when (healthCareService) {
            is HomeVisit -> {
                notification.getFirebaseToken(orgId)
                val result = healthCareServices.insert(healthCareService, orgId) as HomeVisit
                notification.broadcastMessage(orgId, result)
                result
            }
            else -> null
        }

        return Response.status(201).entity(respond).build()
    }

    @DELETE
    @Path("/$ORGIDTYPE/${PART_HEALTHCARESERVICE}s")
    @RolesAllowed("ORG", "ADMIN")
    fun delete(
        @PathParam("orgId") orgId: String
    ) {
        healthCareServices.remove(orgId)
    }

    @POST
    @Path("/$ORGIDTYPE/${PART_HEALTHCARESERVICE}s")
    @RolesAllowed("ORG", "ADMIN")
    fun createList(
        @PathParam("orgId") orgId: String,
        healthCareService: List<HealthCareService>
    ): Response {
        healthCareService.forEach { roleMapIsSync(it) }

        val respond = when (healthCareService.first()) {
            is HomeVisit -> healthCareServices.insert(healthCareService, orgId)
            else -> null
        }

        return Response.status(201).entity(respond).build()
    }

    @GET
    @Path("/$ORGIDTYPE/$PART_HEALTHCARESERVICE/$VISITIDTYPE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun find(@PathParam("orgId") orgId: String, @PathParam("visitId") visitId: String): HomeVisit {

        val result = healthCareServices.find(visitId, orgId)

        return if (result != null)
            result as HomeVisit
        else throw NullPointerException("ไม่พบ ข้อมูลที่ค้นหา")
    }

    @PUT
    @Path("/$ORGIDTYPE/$PART_HEALTHCARESERVICE/$VISITIDTYPE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("visitId") visitId: String,
        healthCareService: HealthCareService
    ): HealthCareService {
        roleMapIsSync(healthCareService)
        return healthCareServices.update(healthCareService, orgId)
    }

    private fun roleMapIsSync(healthCareService: HealthCareService) {
        if (healthCareService.link != null)
            when (context?.getLoginRole()) {
                User.Role.ORG -> healthCareService.link?.isSynced = true
                User.Role.ADMIN -> healthCareService.link?.isSynced = true
                else -> healthCareService.link?.isSynced = false
            }
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/$PART_HEALTHCARESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun getPerson(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): List<HealthCareService> {
        return healthCareServices.findByPatientId(personId, orgId)
    }
}
