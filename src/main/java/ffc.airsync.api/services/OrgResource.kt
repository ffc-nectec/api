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

package ffc.airsync.api.services

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.OrgService
import ffc.entity.Organization
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/org")
class OrgResource {
    // Register orgUuid.
    @Context
    lateinit var req: HttpServletRequest

    @POST
    fun create(organization: Organization): Response {
        printDebug("Org register ${organization.name}")

        printDebug("Create my org")
        var ipAddress = req.getHeader("X-Forwarded-For")
        printDebug("\tGet ip address from header X-Forwarded-For = $ipAddress")
        printDebug("\tGet from req.remoteAddr = ${req.remoteAddr}")
        if (ipAddress == null) {
            ipAddress = req.remoteAddr
        }

        printDebug("\tip address select = $ipAddress")

        organization.bundle["lastKnownIp"] = ipAddress

        val orgUpdate = OrgService.register(organization)
        printDebug("\tGen ip = " + orgUpdate.bundle["lastKnownIp"] + " Org token = " + orgUpdate.bundle["token"])

        printDebug("Create token")

        return Response.status(Response.Status.CREATED).entity(orgUpdate).build()
    }

    @GET
    fun getMy(@QueryParam("my") my: Boolean = false): List<Organization> {
        printDebug("Get org my")
        var ipAddress = req.getHeader("X-Forwarded-For")
        printDebug("\tGet ip address from header X-Forwarded-For = $ipAddress")
        printDebug("\tGet from req.remoteAddr = ${req.remoteAddr}")
        if (ipAddress == null) {
            ipAddress = req.remoteAddr
        }
        printDebug("\tResult Org by ip = $ipAddress + my = $my")

        return if (my) {
            OrgService.getMy(ipAddress)
        } else {
            OrgService.get()
        }
    }

    @RolesAllowed("ORG")
    @DELETE
    @Path("/{orgId:([\\dabcdefABCDEF]+)}")
    fun remove(@PathParam("orgId") orgId: String): Response {
        printDebug("Remove org $orgId")

        OrgService.remove(orgId)

        return Response.status(200).build()
    }
}
