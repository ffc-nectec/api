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

package ffc.airsync.api.services.house

import ffc.airsync.api.resorceCall
import ffc.airsync.api.services.BLOCKTYPE
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.Sync
import ffc.airsync.api.services.util.getLoginRole
import ffc.entity.place.House
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SyncHouseResource : Sync<House> {

    @Context
    private lateinit var context: SecurityContext

    @POST
    @Path("/$ORGIDTYPE/houses/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN", "PROVIDER")
    override fun insertBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        item: List<House>
    ): List<House> {
        return resorceCall { houseService.create(orgId, context.getLoginRole(), item, block) }
    }

    @GET
    @Path("/$ORGIDTYPE/houses/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN", "PROVIDER")
    override fun getBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): List<House> {
        return resorceCall { houses.getBlock(orgId, block) }
    }

    @PUT
    @Path("/$ORGIDTYPE/houses/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN", "PROVIDER")
    override fun confirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        resorceCall { houses.confirmBlock(orgId, block) }
    }

    @DELETE
    @Path("/$ORGIDTYPE/houses/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN", "PROVIDER")
    override fun unConfirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        resorceCall { houses.unConfirmBlock(orgId, block) }
    }
}
