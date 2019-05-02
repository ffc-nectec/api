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

package ffc.airsync.api.services.notification

import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FirebaseResource {

    private val logger = getLogger()

    @POST
    @Path("/$ORGIDTYPE/firebasetoken")
    @RolesAllowed("ADMIN")
    fun updateToken(@PathParam("orgId") orgId: String, firebaseToken: HashMap<String, String>): Response {
        logger.debug("Call update Firebase Token OrgID $orgId Firebase Token = ${firebaseToken["firebasetoken"]}")

        notification.createFirebase(orgId, firebaseToken["firebasetoken"]!!, true)

        return Response.status(200).build()
    }

    @POST
    @Path("/$ORGIDTYPE/mobilefirebasetoken")
    @RolesAllowed("PROVIDER", "SURVEYOR")
    fun createToken(@PathParam("orgId") orgId: String, firebaseToken: HashMap<String, String>): Response {
        logger.debug("Call update Firebase Token by OrgID $orgId Firebase Token = ${firebaseToken["firebasetoken"]}")
        notification.createFirebase(orgId, firebaseToken["firebasetoken"]!!, false)

        return Response.status(200).build()
    }
}
