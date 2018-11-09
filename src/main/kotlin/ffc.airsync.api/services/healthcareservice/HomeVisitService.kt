package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.services.notification.broadcastMessage
import ffc.airsync.api.services.notification.notification
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit

object HomeVisitService {
    fun create(homeVisit: HomeVisit, orgId: String): HomeVisit {
        notification.getFirebaseToken(orgId)
        val result = healthCareServices.insert(homeVisit, orgId) as HomeVisit

        notification.broadcastMessage(orgId, result)

        return result
    }

    fun createListByOrganization(homeVisit: List<HomeVisit>, orgId: String): List<HomeVisit> {
        return healthCareServices.insert(homeVisit, orgId) as List<HomeVisit>
    }

    fun find(orgId: String, id: String): HomeVisit {
        val result = healthCareServices.find(id, orgId)

        return if (result != null)
            result as HomeVisit
        else throw NullPointerException("ไม่พบ ข้อมูลที่ค้นหา")
    }

    fun get(orgId: String): List<HealthCareService> {
        return healthCareServices.get(orgId)
    }

    fun update(healthCareService: HealthCareService, orgId: String): HealthCareService {
        return healthCareServices.update(healthCareService, orgId)
    }

    fun getPersonHealthCare(orgId: String, personId: String): List<HealthCareService> {
        return healthCareServices.findByPatientId(personId, orgId)
    }
}
