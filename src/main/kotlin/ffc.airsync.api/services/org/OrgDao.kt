/*
 * Copyright (c) 2019 NECTEC
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
 *
 */

package ffc.airsync.api.services.org

import ffc.airsync.api.services.Dao
import ffc.entity.Organization

interface OrgDao : Dao {
    fun insert(organization: Organization): Organization

    fun remove(orgId: String)

    fun findById(orgId: String): Organization
    fun findAll(): List<Organization>
    fun find(query: String): List<Organization>
    fun findByIpAddress(ipAddress: String): List<Organization>
}

val orgs: OrgDao by lazy { MongoOrgDao() }

private val dontRecive = Regex(""".*[\:\/\?\#\[\]\@\!\$\&\'\(\)\*\+\,\;\=\<\>\{\}\|\`\^\\\"\% \.].*""")

/**
 * ตรวจสอบ Organization name ว่าอยู่ในเงื่อนไขในการตั้งชื่อหรือไม่
 */
fun Organization.isAcceptOrganizationName(): Boolean = this.name.isAcceptOrganizationName()

internal fun String.isAcceptOrganizationName() = !dontRecive.matches(this)
