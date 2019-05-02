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

package ffc.airsync.api.services.village

import ffc.airsync.api.resorceCall
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.VILLAGETYPE
import ffc.entity.Village
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class VillageResource {
    @POST
    @Path("/$ORGIDTYPE/village")
    @RolesAllowed("ADMIN")
    fun create(@PathParam("orgId") orgId: String, village: Village): Village {
        return resorceCall {
            villages.insert(orgId, village)
        }
    }

    @POST
    @Path("/$ORGIDTYPE/villages")
    @RolesAllowed("ADMIN")
    fun create(@PathParam("orgId") orgId: String, village: List<Village>): List<Village> {
        val output = resorceCall { villages.insert(orgId, village) }
        return output
    }

    @PUT
    @Path("/$ORGIDTYPE/village/$VILLAGETYPE")
    @RolesAllowed("ADMIN")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("villageId") villageId: String,
        village: Village
    ): Village {
        require(villageId == village.id) { "ไม่สามารถ update ได้ เนื่องจาก id ไม่ตรงกับเอกสาร" }
        return resorceCall { villages.update(orgId, village) }
    }

    @DELETE
    @Path("/$ORGIDTYPE/village/$VILLAGETYPE")
    @RolesAllowed("ADMIN")
    fun delete(
        @PathParam("orgId") orgId: String,
        @PathParam("villageId") villageId: String
    ): Response {
        resorceCall { villages.delete(orgId, villageId) }
        return Response.status(Response.Status.OK).build()
    }

    @DELETE
    @Path("/$ORGIDTYPE/villages")
    @RolesAllowed("ADMIN")
    fun deleteOrg(
        @PathParam("orgId") orgId: String
    ): Response {
        resorceCall { villages.removeByOrgId(orgId) }
        return Response.status(Response.Status.OK).build()
    }

    @GET
    @Path("/$ORGIDTYPE/village/$VILLAGETYPE")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun get(
        @PathParam("orgId") orgId: String,
        @PathParam("villageId") villageId: String
    ): Village {
        return resorceCall { villages.get(orgId, villageId) }
    }

    @GET
    @Path("/$ORGIDTYPE/village")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun find(
        @PathParam("orgId") orgId: String,
        @QueryParam("query") query: String?
    ): List<Village> {
        return if (query != null) {
            resorceCall { villages.find(orgId, query) }
        } else {
            villages.find(orgId)
        }
    }
}
