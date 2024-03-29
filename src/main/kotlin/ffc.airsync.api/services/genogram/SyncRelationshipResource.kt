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

package ffc.airsync.api.services.genogram

import ffc.airsync.api.filter.cache.Cache
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.BLOCKTYPE
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Person
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

class SyncRelationshipResource {
    @Context
    private var context: SecurityContext? = null
    private val logger = getLogger()

    @POST
    @Path("/$ORGIDTYPE/person/relationships/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    @Cache(maxAge = 5)
    fun insertBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        relation: Map<String, @JvmSuppressWildcards List<Person.Relationship>>
    ): Map<String, List<Person.Relationship>> {
        try {
            return personRelationsShip.addRelation(orgId, block, relation)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            throw ex
        }
    }

    @GET
    @Path("/$ORGIDTYPE/person/relationships/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    @Cache(maxAge = 5)
    fun getBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Map<String, List<Person.Relationship>> {
        try {
            return personRelationsShip.getBlock(orgId, block)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            throw ex
        }
    }

    @PUT
    @Path("/$ORGIDTYPE/person/relationships/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    @Cache(maxAge = 5)
    fun confirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/relationships/sync/$BLOCKTYPE")
    @RolesAllowed("ADMIN")
    @Cache(maxAge = 5)
    fun unConfirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/relationships/sync/clean")
    @RolesAllowed("ADMIN")
    @Cache(maxAge = 5)
    fun cleanAll(
        @PathParam("orgId") orgId: String
    ) {
    }
}
