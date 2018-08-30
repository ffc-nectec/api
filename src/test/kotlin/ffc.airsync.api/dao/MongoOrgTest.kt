/*
 * Copyright (c) 2018 NECTEC
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

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import ffc.airsync.api.resourceFile
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.parseTo
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not equal`
import org.junit.After
import org.junit.Before
import org.junit.Test

class MongoOrgTest {

    lateinit var dao: OrgDao
    lateinit var client: MongoClient
    lateinit var server: MongoServer

    lateinit var hahahaOrg: Organization

    lateinit var nectecOrg: Organization

    @Before
    fun initDb() {
        server = MongoServer(MemoryBackend())
        val serverAddress = server.bind()
        client = MongoClient(ServerAddress(serverAddress))
        MongoAbsConnect.setClient(client)
        dao = orgs(serverAddress.hostString, serverAddress.port)

        hahahaOrg = dao.insert(Org("รพ.สต.HAHAHA", "203.111.222.123"))
        nectecOrg = dao.insert(Org("รพ.สต.Nectec", "192.168.99.3"))
    }

    @After
    fun cleanDb() {
        client.close()
        server.shutdownNow()
    }

    fun Org(name: String = "NECTEC", ip: String = "127.0.01"): Organization =
            Organization().apply {
                this.name = name
                users.add(User("maxkung", User.Role.ORG))
                users.add(User("cat"))
                bundle["lastKnownIp"] = ip // "203.111.222.123"
            }

    fun User(name: String, role: User.Role = User.Role.USER): User =
            User().apply {
                this.name = name
                password = "catbite"
                this.role = role
            }

    @Test
    fun insert() {
        val returnedOrg = dao.insert(Org("รพสต.AAA"))

        with(returnedOrg) {
            name `should be equal to` "รพสต.AAA"
            isTempId `should be` false
            bundle["lastKnownIp"] = "192.168.99.3"
        }
    }

    @Test
    fun insertFromJson() {
        val org = resourceFile("organization.json").parseTo<Organization>()

        val returnOrg = dao.insert(org)

        with(returnOrg) {
            print(id)
            name `should be equal to` org.name
            link `should equal` org.link
            isTempId `should be` false
        }
    }

    @Test
    fun findAll() {
        val orgs = dao.findAll()

        with(orgs) {
            size `should be equal to` 2
            find { it.name == nectecOrg.name } `should not equal` null
            find { it.name == hahahaOrg.name } `should not equal` null
        }
    }

    @Test
    fun findNectecByIp() {
        val orgs = dao.findByIpAddress("192.168.99.3")

        orgs[0].name `should be equal to` nectecOrg.name
    }

    @Test
    fun findHahahaByIp() {
        val orgs = dao.findByIpAddress("203.111.222.123")

        orgs[0].name `should be equal to` hahahaOrg.name
    }

    @Test
    fun find() {
        dao.findById(nectecOrg.id) `should equal` nectecOrg
        dao.findById(hahahaOrg.id) `should equal` hahahaOrg
    }

    @Test
    fun createAndGetFirebase() {
        dao.createFirebase(nectecOrg.id, "abcdef001", true)
        dao.createFirebase(nectecOrg.id, "abcdef007", false)
        dao.createFirebase(hahahaOrg.id, "abcdef002", true)
        dao.createFirebase(hahahaOrg.id, "abcdef003", false)

        val firebaseNectecList = dao.getFirebaseToken(nectecOrg.id)
        firebaseNectecList.find { it == "abcdef001" } `should not equal` null
        firebaseNectecList.find { it == "abcdef007" } `should not equal` null
        firebaseNectecList.size `should be equal to` 2

        val firebaseHahahaList = dao.getFirebaseToken(hahahaOrg.id)
        firebaseHahahaList.find { it == "abcdef002" } `should not equal` null
        firebaseHahahaList.find { it == "abcdef003" } `should not equal` null
        firebaseHahahaList.size `should be equal to` 2
    }

    @Suppress("UNCHECKED_CAST")
    var Organization.firebaseToken: MutableList<String>
        set(value) {
            bundle.put("firebaseToken", value)
        }
        get() = bundle["firebaseToken"] as MutableList<String>

    @Test
    fun removeFirebase() {
        dao.createFirebase(nectecOrg.id, "abcdef001", true)
        dao.createFirebase(nectecOrg.id, "abcdef007", false)

        dao.createFirebase(hahahaOrg.id, "abcdef002", true)
        dao.createFirebase(hahahaOrg.id, "abcdef003", false)

        dao.removeFirebase(nectecOrg.id, "abcdef001", true)
        val firebaseNectecList = dao.getFirebaseToken(nectecOrg.id)
        firebaseNectecList.find { it == "abcdef001" } `should equal` null

        dao.removeFirebase(hahahaOrg.id, "abcdef003", false)
        val firebaseHahahaList = dao.getFirebaseToken(hahahaOrg.id)
        firebaseHahahaList.forEach {
            println(it)
        }
        firebaseHahahaList.find { it == "abcdef003" } `should equal` null
    }

    @Test
    fun removeIsAffect() {
        dao.remove(nectecOrg.id)

        dao.findAll().size `should be equal to` 1
    }
}
