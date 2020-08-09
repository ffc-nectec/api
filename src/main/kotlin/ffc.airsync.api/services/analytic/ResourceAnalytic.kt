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

import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.entity.healthcare.analyze.HealthAnalyzer
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class ResourceAnalytic {

    @POST
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/healthanalyze")
    @RolesAllowed("ADMIN")
    fun insert(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String,
        healthAnalyzer: HealthAnalyzer
    ): HealthAnalyzer {
        return analyzers.insert(
            orgId = orgId,
            personId = personId,
            healthAnalyzer = healthAnalyzer
        )
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/healthanalyze")
    @RolesAllowed("ADMIN", "PROVIDER")
    fun getByPersonId(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String
    ): HealthAnalyzer {
        return analyzers.getByPersonId(orgId, personId)
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/healthanalyze")
    @RolesAllowed("ADMIN")
    fun delete(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String
    ): Response {
        analyzers.deleteByPersonId(orgId, personId)
        return Response.status(Response.Status.OK).build()
    }

    @DELETE
    @Path("/$ORGIDTYPE/healthanalyze")
    @RolesAllowed("ADMIN")
    fun deleteOrg(
        @PathParam("orgId") orgId: String
    ): Response {
        analyzers.removeByOrgId(orgId)
        return Response.status(Response.Status.OK).build()
    }
}
