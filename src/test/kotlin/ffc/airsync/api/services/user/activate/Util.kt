/*
 * Copyright (c) 2562 NECTEC
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

package ffc.airsync.api.services.user.activate

import ffc.airsync.api.services.user.UserDao
import ffc.entity.Link
import ffc.entity.Organization
import ffc.entity.System
import ffc.entity.User

internal fun UserDao.adduser(org: Organization) {
    listOf(
        createUser("somChai"),
        createUser("adm"),
        createUser("ADM"),
        createUser("newuser"),
        createUser("usr_db"),
        createUser("Drug_Store_Admin")
    ).forEach {
        insertUser(it, org.id)
    }
}

internal fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
    Organization().apply {
        this.name = name
        users.add(createUser("maxkung", User.Role.ADMIN))
        users.add(createUser("somYing"))
        users.add(createUser("bingSu"))
        users.add(createUser("manman"))
        bundle["lastKnownIp"] = ip // "203.111.222.123"
        link = Link(System.JHICS)
    }

private fun createUser(name: String, role: User.Role = User.Role.PROVIDER): User =
    User().apply {
        this.name = name
        password = "catbite"
        this.roles.add(role)
    }
