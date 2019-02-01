package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.airsync.api.services.VISITIDTYPE
import ffc.airsync.api.services.analytic.analyzers
import ffc.airsync.api.services.notification.broadcastMessage
import ffc.airsync.api.services.notification.notification
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
        val loginRole = context.getLoginRole()
        if (!(User.Role.ADMIN inRole loginRole || User.Role.ORG inRole loginRole)) {
            require(healthCareService.link == null) { "สร้าง healthCareService จาก User ต้องไม่มี link" }
        } else {
            require(healthCareService.link != null) { "ORG, ADMIN จำเป็นต้องมีข้อมูล link " }
        }

        `ตรวจสอบข้อมูลการเยี่ยมบ้านใหม่`(healthCareService)

        roleMapIsSync(healthCareService)
        // notification.getFirebaseToken(orgId)
        val result = healthCareServices.insert(healthCareService, orgId)
        notification.broadcastMessage(orgId, result)

        visitAnalyzer(healthCareService, orgId)
        return result
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
        require(visitId == healthCareService.id) { "รหัส ID การ Update ไม่ตรงกัน" }
        val result = healthCareServices.update(healthCareService, orgId)
        notification.broadcastMessage(orgId, result)
        visitAnalyzer(healthCareService, orgId)
        return result
    }

    private fun visitAnalyzer(healthCareService: HealthCareService, orgId: String) {
        val analyzer = HealthAnalyzer()
        val personId = healthCareService.patientId
        analyzer.analyze(*healthCareServices.getByPatientId(orgId, personId).toTypedArray())
        analyzers.insert(orgId, personId, analyzer)
    }

    @DELETE
    @Path("/$ORGIDTYPE/${PART_HEALTHCARESERVICE}s")
    @RolesAllowed("ORG", "ADMIN")
    fun delete(
        @PathParam("orgId") orgId: String
    ) {
        healthCareServices.removeByOrgId(orgId)
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
        return healthCareServices.get(visitId, orgId) ?: throw NullPointerException("ไม่พบ ข้อมูลที่ค้นหา")
    }

    private fun roleMapIsSync(healthCareService: HealthCareService) {
        val role = context.getLoginRole()
        when {
            User.Role.ORG inRole role -> {
                healthCareService.link!!.isSynced = true
            }
            User.Role.ADMIN inRole role -> {
                healthCareService.link!!.isSynced = true
            }
            else -> {
                healthCareService.link =
                    if (healthCareService.link == null) Link(System.JHICS) else healthCareService.link
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
        return healthCareServices.getByPatientId(orgId, personId)
    }

    private fun `ตรวจสอบข้อมูลการเยี่ยมบ้านใหม่`(healthCareService: HealthCareService) {
        val patientId = healthCareService.patientId
        val providerId = healthCareService.providerId
        healthCareService.specialPPs.forEach {
            require(it.isTempId) { "SpecialPP id ต้องเป็น TempId" }
            require(it.patientId == patientId) { "patientId ผิดพลาด ${it.patientId} != $patientId" }
            require(it.providerId == providerId) { "providerId ผิดพลาด ${it.providerId} != $providerId" }
        }
        healthCareService.communityServices.forEach {
            require(it.isTempId) { "CommunityService id ต้องเป็น TempId" }
        }
        healthCareService.ncdScreen?.let {
            require(it.isTempId) { "Ncd Screen id ต้องเป็น TempId" }
        }
    }
}
