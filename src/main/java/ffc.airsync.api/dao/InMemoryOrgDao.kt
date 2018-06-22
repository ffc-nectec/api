/*
 * Copyright (c) 2561 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.Organization
import ffc.entity.toJson
import java.util.*
import javax.ws.rs.NotFoundException

class InMemoryOrgDao : OrgDao {
    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private constructor()

    val pcuList = arrayListOf<Organization>()


    companion object {
        val instance = InMemoryOrgDao()
        private var i: Int = 0

    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        findByUuid(orgUUID)
        pcuList.removeIf { it.uuid == orgUUID }

    }

    override fun findById(id: String): Organization {
        return pcuList.find { it.id == id } ?: throw NotFoundException()
    }

    override fun findByToken(token: UUID): Organization {

        return pcuList.find { it.token == token } ?: throw NotFoundException()
    }


    override fun insert(organization: Organization) {

        pcuList.removeIf { it.uuid == organization.uuid }


        if (!pcuList.contains(organization)) {
            printDebug("Organization insert InMemoryOrgDao \nOrganization data = " + organization.toJson())
            organization.id = (i++.toString())
            pcuList.add(organization)
            printDebug("Test get Organization Before insert\nOrganization data =" + findByUuid(organization.uuid).toJson())
        } else {
            pcuList.remove(organization)
            insert(organization)
        }
    }

    override fun findByUuid(uuid: UUID): Organization {
        printDebug("findByUuid InMemoryOrgDao \nUUID data = " + uuid)
        val pcu = pcuList.find { it.uuid == uuid } ?: throw NotFoundException()
        printDebug("find Result = " + pcu.toJson())
        return pcu
        //return pcuList.findCall { it.uuid }
    }

    override fun findByIpAddress(ipAddress: String): List<Organization> {

        val orgList = arrayListOf<Organization>()




        pcuList.forEach {
            if (it.lastKnownIp == ipAddress) {
                orgList.add(it)
            }
        }
        if (orgList.size < 1) throw NotFoundException()
        return orgList
    }

    override fun remove(organization: Organization) {
        //pcuList.remove(organization)
        pcuList.removeIf { it.uuid == organization.uuid }
    }

    override fun find(): List<Organization> {
        return pcuList.toList()
    }

    override fun updateToken(organization: Organization): Organization {
        val pcuFind = findByUuid(organization.uuid)
        pcuFind.token = UUID.randomUUID()
        insert(pcuFind)
        return pcuFind
    }
}
