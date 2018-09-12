package ffc.airsync.api.services.module

import com.google.firebase.messaging.Message
import ffc.entity.healthcare.HomeVisit
import javax.ws.rs.NotFoundException

object HomeVisitService {
    fun create(homeVisit: HomeVisit, orgId: String): HomeVisit {

        val firebaseToken = orgDao.getFirebaseToken(orgId)
        val result = healthCareServices.insert(homeVisit) as HomeVisit

        Message.builder().broadcastVisit(result, firebaseToken, orgId)

        return result
    }

    fun get(orgId: String, id: String): HomeVisit {
        val result = healthCareServices.find(id)

        return if (result != null)
            result as HomeVisit
        else throw NotFoundException("ไม่พบ ข้อมูลที่ค้นหา")
    }
}
