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

package ffc.airsync.api.services.analytic

import ffc.airsync.api.services.BLOCKTYPE
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.healthcare.analyze.HealthAnalyzer
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SyncResourceAnalytic {

    @POST
    @Path("/$ORGIDTYPE/healthanalyzes/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    fun insertBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        healthAnalyzer: Map<String, HealthAnalyzer>
    ): Map<String, HealthAnalyzer> {
        return analyzers.insertBlock(
            orgId = orgId,
            block = block,
            healthAnalyzer = healthAnalyzer
        )
    }

    @GET
    @Path("/$ORGIDTYPE/healthanalyzes/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    fun getBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Map<String, HealthAnalyzer> {
        return analyzers.getBlock(
            orgId = orgId,
            block = block
        )
    }

    @PUT
    @Path("/$ORGIDTYPE/healthanalyzes/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    fun confirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        analyzers.confirmBlock(
            orgId = orgId,
            block = block
        )
    }

    @DELETE
    @Path("/$ORGIDTYPE/healthanalyzes/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    fun unConfirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {

        analyzers.unConfirmBlock(
            orgId = orgId,
            block = block
        )
    }
}
