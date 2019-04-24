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

package ffc.airsync.api.services.user.activate

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.airsync.api.services.org.OrgDao
import ffc.airsync.api.services.user.MongoUserDao
import ffc.airsync.api.services.user.UserDao
import ffc.entity.Organization
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoActivateDaoTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    lateinit var orgDao: OrgDao
    lateinit var activateDao: ActivateDao
    lateinit var userDao: UserDao

    lateinit var docOrg: Organization
    lateinit var nectecOrg: Organization

    lateinit var docUser: List<User>
    lateinit var nectecUser: List<User>

    @Before
    fun setUp() {
        orgDao = MongoOrgDao()
        docOrg = orgDao.insert(Org("รพสตHAHAHA", "203.111.222.123").apply {
            displayName = "รพ.สต.HAHAHA"
            tel = "02-388-5555"
            address = "166 ม.99 ต.เนคเทค อ.อยู่ดี จ.กินดี"
            link!!.keys["pcucode"] = "100145"
        })

        nectecOrg = orgDao.insert(Org("รพสตNectec", "192.168.99.3").apply {
            displayName = "รพ.สต.Nectec"
            tel = "037-261-044"
            address = "161 ม.29 ต.สง่างาม อ.สดใส จ.ผิวผ่อง"
            link!!.keys["pcucode"] = "203"
        })

        userDao = MongoUserDao()
        userDao.adduser(docOrg)
        userDao.adduser(nectecOrg)

        docUser = userDao.findUser(docOrg.id)
        nectecUser = userDao.findUser(nectecOrg.id)

        activateDao = MongoActivateDao()
    }

    @Test
    fun setActivate() {
        val user = docUser.last()
        user.isActivated `should be equal to` false

        val userActivate = activateDao.setActivate(docOrg.id, user.id)

        userActivate.isActivated `should be equal to` true
    }

    @Test
    fun checkActivate() {
        val user = docUser.last()
        activateDao.setActivate(docOrg.id, user.id)

        activateDao.checkActivate(docOrg.id, user.id) `should be equal to` true
    }
}
