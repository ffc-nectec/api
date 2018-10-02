package ffc.airsync.api.services.healthcareservice

import com.google.firebase.messaging.Message
import ffc.airsync.api.services.notification.broadcastVisit
import ffc.airsync.api.services.notification.notification
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import javax.ws.rs.NotFoundException

object HomeVisitService {
    fun create(homeVisit: HomeVisit, orgId: String): HomeVisit {
        val firebaseToken = notification.getFirebaseToken(orgId)
        val result = healthCareServices.insert(homeVisit, orgId) as HomeVisit

        Message.builder().broadcastVisit(result, firebaseToken, orgId)

        return result
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
        val result = healthCareServices.findByPatientId(personId, orgId)
        return if (result.isNotEmpty()) result else throw NotFoundException("ค้นหา health care service person id $personId ไม่พบ")
    }
}
