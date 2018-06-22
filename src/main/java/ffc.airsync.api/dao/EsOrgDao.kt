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
import ffc.entity.fromJson
import ffc.entity.toJson
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import java.net.InetAddress
import java.util.*
import kotlin.collections.ArrayList

class EsOrgDao : OrgDao {

    var client: TransportClient = PreBuiltTransportClient(Settings.EMPTY)
            .addTransportAddress(TransportAddress(InetAddress.getByName("127.0.0.1"), 9300))

    override fun insert(organization: Organization) {
        client.insert("airsync", "air", organization.uuid.toString(), organization.toJson())
        organization.lastKnownIp?.let { client.insert("lastKnownIp", "ip", it, organization.toJson()) }//อนาคตต้องเก็บเป็นแบบ list ป้องกัน IP ซ้ำ   ดึงค่ามาอ่าน ตรวจสอบ uuid ถ้าซ้ำ update ถ้าคนละ uuid ให้เพิ่มใน List
        organization.session?.let { client.insert("session", "sess", it, organization.toJson()) }
        organization.token?.let { client.insert("token", "token", it.toString(), organization.toJson()) }
    }

    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findById(id: String): Organization {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(organization: Organization) {
        client.delete("airsync", "air", organization.uuid.toString())

        organization.lastKnownIp?.let { client.delete("lastKnownIp", "ip", it) }
        organization.session?.let { client.delete("session", "sess", it) }
        organization.token?.let { client.delete("token", "token", it.toString()) }
    }

    override fun findByUuid(uuid: UUID): Organization {
        var response = client.get("airsync", "air", uuid.toString())
        printDebug(response.sourceAsString)
        return response.sourceAsString.fromJson()
    }

    override fun findByToken(token: UUID): Organization {
        val response = client.get("token", "token", token.toString())
        return response.sourceAsString.fromJson()

    }

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        var response = client.get("lastKnownIp", "ip", ipAddress)
        printDebug(response.sourceAsString)
        return response.sourceAsString.fromJson()
    }

    override fun find(): List<Organization> {
        var response = client.search("airsync", "air")
        val pcuList = ArrayList<Organization>()

        response.hits.hits.forEach { result -> pcuList.add(result.sourceAsString.fromJson()) }
        return pcuList
    }

    override fun updateToken(organization: Organization): Organization {
        val pcuFind = findByUuid(organization.uuid)
        pcuFind.token = UUID.randomUUID()
        insert(pcuFind)
        return pcuFind
    }
}

