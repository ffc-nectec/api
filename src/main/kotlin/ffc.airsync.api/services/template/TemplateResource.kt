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

package ffc.airsync.api.services.template

import ffc.airsync.api.filter.cache.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Template
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TemplateResource {

    @GET
    @Path("/$ORGIDTYPE/template")
    @Cache(maxAge = 3600)
    fun find(
        @PathParam("orgId") orgId: String,
        @QueryParam("query") query: String?
    ): List<Template> {
        return templates.find(orgId, query ?: "")
    }

    @POST
    @Path("/$ORGIDTYPE/template")
    @RolesAllowed("ADMIN")
    fun clearAndInsert(
        @PathParam("orgId") orgId: String,
        template: List<Template>
    ): List<Template> {
        templates.removeByOrgId(orgId)
        templates.insert(orgId, template)
        return template
    }
}
