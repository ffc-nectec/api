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

package ffc.airsync.api.services.org

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.util.forwardForIpAddress
import ffc.airsync.api.services.util.getUserLogin
import ffc.airsync.api.services.util.realIpAddress
import ffc.entity.Organization
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

@Path("/org")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class OrgResource {

    @Context
    private var context: SecurityContext? = null

    companion object {
        val logger = getLogger()
    }

    @Context
    lateinit var req: HttpServletRequest

    @POST
    fun create(organization: Organization): Response {
        organization.users.forEach {
            it.roles.add(it.role)
        }
        val org = OrgService.register(organization.apply { bundle["lastKnownIp"] = req.forwardForIpAddress() })
        logger.info("Register organization Name: ${org.name} Id:${org.id}")
        return Response.status(201).entity(org).build()
    }

    @GET
    @Cache(maxAge = 5)
    fun get(
        @QueryParam("my") @DefaultValue("false") my: Boolean,
        @QueryParam("query") query: String?
    ): List<Organization> {
        return if (my) {
            logger.debug("Find Organization with ip-address = ${req.forwardForIpAddress()}")
            OrgService.getMy("Forward ip ${req.forwardForIpAddress()} Real IP ${req.realIpAddress()}")
        } else {
            val queryFind = query ?: ""
            if (queryFind.isNotEmpty()) {
                require(!queryFind.contains(Regex("""[\,\%\!\?\'\|\*]"""))) {
                    "ในการค้นหาไม่ควรมีอักขระ , % ! ? ' | *"
                }

                OrgService.find(queryFind)
            } else
                OrgService.get()
        }
    }

    @DELETE
    @Path("/$ORGIDTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun remove(@PathParam("orgId") orgId: String): Response {
        logger.info("Remove organization by user ${context!!.getUserLogin()} Organization id: $orgId")
        OrgService.remove(orgId)
        return Response.status(200).build()
    }
}
