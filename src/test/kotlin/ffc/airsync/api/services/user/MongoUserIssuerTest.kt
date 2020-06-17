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

package ffc.airsync.api.services.user

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.resourceFile
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.parseTo
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * ทดสอบกับฐานคลองหลวงแล้ว Error พบ user ซ้ำ
 */
class MongoUserIssuerTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private lateinit var nectecOrg: Organization
    private lateinit var dao: MongoUserDao

    val userList2 = resourceFile("user.json").parseTo<List<User>>()

    @Before
    fun initDb() {
        dao = MongoUserDao()
        val org = Org("รพสตNectec", "192.168.99.3")
        nectecOrg = MongoOrgDao().insert(org)
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
        Organization().apply {
            this.name = name
            bundle["lastKnownIp"] = ip // "203.111.222.123"
            users = listOf(User("maxkung", User.Role.ADMIN)).toMutableList()
        }

    fun User(name: String, role: User.Role = User.Role.PROVIDER): User =
        User().apply {
            this.name = name
            password = "catbite"
            this.roles.add(role)
        }

    @Test
    fun insertListUser() {
        val result = userList2.map { dao.insert(it, nectecOrg.id) }

        result.size `should be equal to` 2
    }
}
