package ffc.airsync.api.services.org

import ffc.airsync.api.getLogger
import ffc.airsync.api.services.analytic.analyzers
import ffc.airsync.api.services.healthcareservice.healthCareServices
import ffc.airsync.api.services.house.houses
import ffc.airsync.api.services.person.persons
import ffc.airsync.api.services.token.tokens
import ffc.airsync.api.services.village.villages
import ffc.entity.Organization
import ffc.entity.gson.toJson

internal object OrgService {
    val logger = getLogger()
    fun register(organization: Organization): Organization {
        logger.debug("\t\tCall mongo insert organization ${organization.toJson()}")
        return orgs.insert(organization)
    }

    fun remove(orgId: String) {
        logger.debug("Remove org 1/7")
        orgs.remove(orgId)
        logger.debug("Remove analyzers 1/2")
        analyzers.removeByOrgId(orgId)
        logger.debug("Remove healthCare 1/3")
        healthCareServices.removeByOrgId(orgId)
        logger.debug("Remove house 1/4")
        houses.removeByOrgId(orgId)
        logger.debug("Remove persons 1/5")
        persons.remove(orgId)
        logger.debug("Remove villages 1/6")
        villages.removeByOrgId(orgId)
        logger.debug("Remove token 1/7")
        tokens.removeByOrgId(orgId)
    }

    fun getMy(ipAddress: String): List<Organization> {
        logger.info("Organization by ip: $ipAddress")
        val orgList = orgs.findByIpAddress(ipAddress)
        val orgReturn = hiddenPrivate(orgList)
        if (orgReturn.isEmpty()) throw NullPointerException("ไม่มีข้อมูลลงทะเบียน")
        return orgReturn
    }

    /**
     * ซ่อนข้อมูลสำคัญก่อน Return ค่ากลับไป
     */
    private fun hiddenPrivate(orgList: List<Organization>): List<Organization> {
        orgList.forEach {
            it.users.removeIf { true }
            it.link = null
            it.bundle.remove("lastKnownIp")
        }
        return orgList
    }

    /**
     * เรียกดูรายชื่อ Organization ที่ลงทะเบียนไว้ทั้งหมด
     */
    fun get(): List<Organization> {
        val orgList = orgs.findAll()
        if (orgList.isEmpty()) throw NullPointerException("ไม่มีข้อมูลลงทะเบียน")
        return hiddenPrivate(orgList)
    }

    fun find(query: String): List<Organization> {
        val result = hiddenPrivate(orgs.find(query))
        return if (result.isNotEmpty()) result else throw NullPointerException("ค้นหา $query ไม่พบ")
    }
}
