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

package ffc.airsync.api

import ffc.airsync.api.services.org.orgs
import ffc.airsync.api.services.user.users
import ffc.entity.Organization
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.junit.Ignore
import org.junit.Test

@Ignore("ทดสอบกับเครื่อง Dev เท่านั้น")
class RealTest {

    lateinit var nectecOrg: Organization

    @Test
    fun insertUser() {
        nectecOrg = orgs.insert(Org("รพ.สต.Nectec", "192.168.99.3"))
        val user = users.insert(User("Sommai"), nectecOrg.id)
        println(user.toString())
        user.name `should be equal to` "Sommai"
        orgs.remove(nectecOrg.id)
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
        Organization().apply {
            this.name = name
            users.add(User("somYing"))
            users.add(User("somChai"))
            users.add(User("adm"))
            users.add(User("ADM"))
            users.add(User("newuser"))
            users.add(User("usr_db"))
            users.add(User("Drug_Store_Admin"))
            bundle["lastKnownIp"] = ip // "203.111.222.123"
        }

    fun User(name: String, role: User.Role = User.Role.PROVIDER): User =
        User().apply {
            this.name = name
            password = "catbite"
            roles.add(role)
        }
}
