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

import ffc.airsync.api.services.ORGIDTYPE
import java.util.Date
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class OtpResource(
    val otpDao: OtpDao = otp
) {
    @GET
    @Path("/$ORGIDTYPE/otp")
    @RolesAllowed("ORG", "ADMIN")
    fun get(@PathParam("orgId") orgId: String): Map<String, String> {
        return mapOf("otp" to otpDao.get(orgId))
    }

    @POST
    @Path("/$ORGIDTYPE/otp")
    @RolesAllowed("USER", "PROVIDER", "SURVEYOR", "PATIENT")
    fun validate(
        @PathParam("orgId") orgId: String,
        clientOtp: Map<String, String>
    ): Map<String, Boolean> {
        val timestamp = Date(System.currentTimeMillis())
        val otpString = clientOtp.getValue("otp")
        val check = otpDao.isValid(
            orgId = orgId,
            otp = otpString,
            timestamp = timestamp
        )
        if (check) return mapOf("isValid" to true)
        else throw NotAuthorizedException("Cannot auth otp.")
    }
}
