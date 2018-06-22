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


import ffc.airsync.api.dao.UserDao.Companion.checkBlockUser
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.UserStor
import java.util.*

class InMemoryUserDao : UserDao {
    private constructor()

    val userList = arrayListOf<UserStor>()

    companion object {
        val INSTANT = InMemoryUserDao()
    }

    override fun removeByOrgUuid(orgUUID: UUID) {
        userList.removeIf {
            it.orgUuid == orgUUID
        }
    }

    override fun insert(user: User, org: Organization) {
        userList.add(UserStor(user = user, orgUuid = UUID.fromString(org.uuid.toString()), orgId = org.id!!))
    }

    override fun find(orgUuid: UUID): List<UserStor> {

        val userOrg = userList.filter {
            it.orgUuid == orgUuid
        }
        return userOrg
    }

    override fun findById(id: String): List<UserStor> {
        val userOrg = userList.filter { it.orgId == id }
        return userOrg

    }


    override fun isAllow(user: User, orgUuid: UUID): Boolean {
        checkBlockUser(user)
        val user = userList.find {
            it.orgUuid == orgUuid &&
                    it.user.username == user.username &&
                    it.user.password == user.password
        }
        return user != null
    }

    override fun isAllowById(user: User, orgId: String): Boolean {
        checkBlockUser(user)
        val user = userList.find {
            it.orgId == orgId &&
                    it.user.username == user.username &&
                    it.user.password == user.password
        }
        return user != null
    }
}
