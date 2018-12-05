package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.airsync.api.services.VISITIDTYPE
import ffc.airsync.api.services.analytic.analyzers
import ffc.airsync.api.services.notification.broadcastMessage
import ffc.airsync.api.services.notification.notification
import ffc.airsync.api.services.person.persons
import ffc.airsync.api.services.util.getLoginRole
import ffc.airsync.api.services.util.inRole
import ffc.entity.Link
import ffc.entity.System
import ffc.entity.User
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.analyze.HealthAnalyzer
import javax.annotation.security.RolesAllowed
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext

const val PART_HEALTHCARESERVICE = "healthcareservice"

@Path("/org")
class HealthCareServiceResource {

    @Context
    private lateinit var context: SecurityContext

    @POST
    @Path("/$ORGIDTYPE/$PART_HEALTHCARESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    fun create(
        @PathParam("orgId") orgId: String,
        healthCareService: HealthCareService
    ): HealthCareService {
        roleMapIsSync(healthCareService)
        notification.getFirebaseToken(orgId)
        val result = healthCareServices.insert(healthCareService, orgId)
        notification.broadcastMessage(orgId, result)

        val analyzer = HealthAnalyzer()
        val personId = healthCareService.patientId
        val houseId = persons.findHouseId(orgId, personId)
        analyzer.analyze(*healthCareServices.findByPatientId(orgId, personId).toTypedArray())
        analyzers.insertAndRepeat(orgId, personId, houseId, analyzer)
        return result
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
    ): List<HealthCareService> {
        healthCareService.forEach { roleMapIsSync(it) }
        return healthCareServices.insert(healthCareService, orgId)
    }

    @GET
    @Path("/$ORGIDTYPE/$PART_HEALTHCARESERVICE/$VISITIDTYPE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun find(@PathParam("orgId") orgId: String, @PathParam("visitId") visitId: String): HealthCareService {
        return healthCareServices.find(visitId, orgId) ?: throw NullPointerException("ไม่พบ ข้อมูลที่ค้นหา")
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
        val role = context.getLoginRole()
        when {
            User.Role.ORG inRole role -> {
                require(healthCareService.link != null) { "จำเป็นต้องมีข้อมูล link " }
                healthCareService.link!!.isSynced = true
            }
            User.Role.ADMIN inRole role -> {
                require(healthCareService.link != null) { "จำเป็นต้องมีข้อมูล link " }
                healthCareService.link!!.isSynced = true
            }
            else -> {
                healthCareService.link = Link(System.JHICS)
                healthCareService.link!!.isSynced = false
            }
        }
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/$PART_HEALTHCARESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun getPerson(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String
    ): List<HealthCareService> {
        return healthCareServices.findByPatientId(orgId, personId)
    }
}
