package ffc.airsync.api.services.org

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.houses
import ffc.airsync.api.services.person.persons
import ffc.airsync.api.services.token.tokens
import ffc.entity.Organization
import ffc.entity.gson.toJson
import javax.ws.rs.NotFoundException

internal object OrgService {
    fun register(organization: Organization): Organization {
        printDebug("\t\tCall mongo insert organization ${organization.toJson()}")
        return orgs.insert(organization)
    }

    fun remove(orgId: String) {
        orgs.remove(orgId)
        houses.removeByOrgId(orgId)
        tokens.removeByOrgId(orgId)
        persons.remove(orgId)
    }

    fun getMy(ipAddress: String): List<Organization> {
        printDebug("Get my org $ipAddress")
        val orgList = orgs.findByIpAddress(ipAddress)
        val orgReturn = hiddenPrivate(orgList)
        if (orgReturn.isEmpty()) throw NotFoundException("ไม่มีข้อมูลลงทะเบียน")
        return orgReturn
    }

    private fun hiddenPrivate(orgList: List<Organization>): List<Organization> {
        orgList.forEach {
            it.users.removeIf { true }
            it.link = null
            it.bundle.remove("lastKnownIp")
        }
        return orgList
    }

    fun get(): List<Organization> {
        printDebug("Get all org")
        val orgList = orgs.findAll()
        val orgReturn = hiddenPrivate(orgList)
        if (orgReturn.isEmpty()) throw NotFoundException("ไม่มีข้อมูลลงทะเบียน")
        return orgReturn
    }

    fun find(query: String): List<Organization> {
        val result = hiddenPrivate(orgs.find(query))
        return if (result.isNotEmpty()) result else throw NotFoundException("ค้นหา $query ไม่พบ")
    }
}
