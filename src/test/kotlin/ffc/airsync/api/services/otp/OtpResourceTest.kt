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

package ffc.airsync.api.services.otp

import com.nhaarman.mockitokotlin2.eq
import ffc.airsync.api.GsonJerseyProvider
import ffc.airsync.api.filter.RequireError
import ffc.airsync.api.filter.SuccessToCreatedResponse
import org.amshove.kluent.When
import org.amshove.kluent.`should equal`
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.mock
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.Test
import java.util.Date
import javax.ws.rs.core.Application
import javax.ws.rs.core.MediaType

class OtpResourceTest : JerseyTest() {

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    val timestamp = Date(System.currentTimeMillis())
    private lateinit var mouckOtpDao: OtpDao

    override fun configure(): Application {
        mouckOtpDao = mock()
        When calling mouckOtpDao.isValid(eq("5bbd7f5ebc920637b04c7796"), eq("123456"), any()) itReturns true
        When calling mouckOtpDao.isValid(eq("5bbd7f5ebc920637b04c7796"), eq("654321"), any()) itReturns false

        return ResourceConfig()
            .registerClasses(RequireError::class.java)
            .register(GsonJerseyProvider::class.java)
            .register(SuccessToCreatedResponse::class.java)
            .register(OtpResource(mouckOtpDao))
    }

    @Test
    fun validateIsNotValid() {
        val otp = mapOf("otp" to "654321")
        val res = target("org/$ORG_ID/otp").request().post(
            javax.ws.rs.client.Entity.entity(otp, MediaType.APPLICATION_JSON_TYPE)
        )

        res.status `should equal` 401
    }

    @Test
    fun validateIsValid() {
        val otp = mapOf("otp" to "123456")
        val res = target("org/$ORG_ID/otp").request().post(
            javax.ws.rs.client.Entity.entity(otp, MediaType.APPLICATION_JSON_TYPE)
        )

        res.readEntity(Map::class.java)["isValid"] `should equal` true
        res.status `should equal` 201
    }
}