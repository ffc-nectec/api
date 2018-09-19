package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.Organization
import ffc.entity.copy
import ffc.entity.gson.toJson
import javax.ws.rs.NotFoundException

object OrgService {
    fun register(organization: Organization): Organization {
        printDebug("\t\tCall mongo insert organization ${organization.toJson()}")
        return orgs.insert(organization)
    }

    fun remove(orgId: String) {
        // val org = orgs.find(orgId)

        // printDebug("Remove org id = $orgId == ${org.id}")
        // if (org.id != orgId) throw NotAuthorizedException("ไม่เจอ Org")

        orgs.remove(orgId)
        // users.removeByOrgId(orgId)
        houses.removeByOrgId(orgId)
        tokens.removeByOrgId(orgId)
        persons.removeGroupByOrg(orgId)
    }

    fun getMy(ipAddress: String): List<Organization> {
        printDebug("Get my org $ipAddress")
        val orgList = orgs.findByIpAddress(ipAddress)
        val orgReturn = hiddenPrivate(orgList)
        if (orgReturn.isEmpty()) throw NotFoundException("ไม่มีข้อมูลลงทะเบียน")
        return orgReturn
    }

    private fun hiddenPrivate(orgList: List<Organization>): ArrayList<Organization> {
        val orgReturn = arrayListOf<Organization>()

        orgList.forEach {
            val org = it.copy<Organization>()
            org.users.removeIf { true }
            org.link = null
            org.bundle.remove("lastKnownIp")
            orgReturn.add(org)
            printDebug("\tOrg list Name ${it.name}")
        }
        return orgReturn
    }

    fun get(): List<Organization> {
        printDebug("Get all org")
        val orgList = orgs.findAll()
        val orgReturn = hiddenPrivate(orgList)
        if (orgReturn.isEmpty()) throw NotFoundException("ไม่มีข้อมูลลงทะเบียน")
        return orgReturn
    }
}
