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

package ffc.airsync.api.services

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.filter.Cache
import ffc.airsync.api.services.module.OrgService
import ffc.entity.Organization
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.BadRequestException
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
    @Context
    lateinit var req: HttpServletRequest
    val HttpServletRequest.ipAddress: String
        get() = getHeader("X-Forwarded-For") ?: remoteAddr

    @POST
    fun create(organization: Organization): Response {
        try {
            organization.users.forEach {
                it.roles.add(it.role)
            }
            val org = OrgService.register(organization.apply { bundle["lastKnownIp"] = req.ipAddress })
            return Response.status(201).entity(org).build()
        } catch (ex: IllegalArgumentException) {
            throw BadRequestException("ไม่สามารถลงทะเบียนได้เนื่องจากหน่วยงานซ้ำ")
        }
    }

    @Cache(maxAge = 5)
    @GET
    fun getMy(@QueryParam("my") my: Boolean = false, @QueryParam("query") query: String?): List<Organization> {
        return if (my) {
            printDebug("Find Organization with ip-address = ${req.ipAddress}")
            OrgService.getMy(req.ipAddress)
        } else {
            val queryFind = query ?: ""
            if (queryFind.isNotEmpty())
                OrgService.find(queryFind)
            else
                OrgService.get()
        }
    }

    @RolesAllowed("ORG", "ADMIN")
    @DELETE
    @Path("/{orgId:([\\dabcdefABCDEF]+)}")
    fun remove(@PathParam("orgId") orgId: String): Response {
        printDebug("Remove org $orgId")
        OrgService.remove(orgId)
        return Response.status(200).build()
    }
}
