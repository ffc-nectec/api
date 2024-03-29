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

package ffc.airsync.api.services.person

import ffc.airsync.api.filter.cache.Cache
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.airsync.api.services.analytic.analyzers
import ffc.airsync.api.services.disease.findIcd10
import ffc.airsync.api.services.util.getLoginRole
import ffc.airsync.api.services.util.inRole
import ffc.airsync.api.services.util.paging
import ffc.entity.Person
import ffc.entity.User
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class PersonResource {
    @Context
    private var context: SecurityContext? = null
    val logger = getLogger()

    @POST
    @Path("/$ORGIDTYPE/persons")
    @RolesAllowed("ADMIN")
    fun creates(@PathParam("orgId") orgId: String, personList: List<Person>): Response {
        personList.forEach { person ->
            mapDeadIcd10(person)
        }
        val persons = persons.insert(orgId, personList)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    private fun mapDeadIcd10(person: Person) {
        person.death?.causes?.findIcd10()?.let {
            person.death = Person.Death(person.death!!.date, it)
        }
    }

    @DELETE
    @Path("/$ORGIDTYPE/persons")
    @RolesAllowed("ADMIN")
    fun delete(@PathParam("orgId") orgId: String): Response {
        persons.remove(orgId)
        return Response.status(Response.Status.OK).build()
    }

    @POST
    @Path("/$ORGIDTYPE/person")
    @RolesAllowed("ADMIN")
    fun create(@PathParam("orgId") orgId: String, person: Person): Response {
        mapDeadIcd10(person)
        val persons = persons.insert(orgId, person)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    @PUT
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE")
    @RolesAllowed("ADMIN", "PROVIDER")
    @Cache(maxAge = 5)
    fun updatePerson(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String,
        person: Person
    ): Person {
        require(person.id == personId) { "ข้อมูลการ Update ไม่ถูกต้อง" }
        val role = context?.getLoginRole()
        when {
            User.Role.ADMIN inRole role -> person.link?.isSynced = true
            else -> person.link?.isSynced = false
        }
        return persons.update(orgId, person)
    }

    /**
     * อัพเดทรายการคน
     */
    @PUT
    @Path("/$ORGIDTYPE/persons")
    @RolesAllowed("ADMIN", "PROVIDER")
    @Cache(maxAge = 5)
    fun updatePersons(
        @PathParam("orgId") orgId: String,
        persons: List<Person>
    ): List<Person> {
        return persons.mapNotNull {
            val run = runCatching {
                require(!it.isTempId) { "ข้อมูลที่จะ update ห้ามเป็น Temp id" }
                updatePerson(orgId, it.id, it)
            }
            if (run.isFailure) logger.warn(run.getOrThrow())
            run.getOrNull()
        }
    }

    @GET
    @Path("/$ORGIDTYPE/person")
    @RolesAllowed("ADMIN", "PROVIDER")
    @Cache(maxAge = 5)
    fun get(
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int,
        @PathParam("orgId") orgId: String,
        @QueryParam("query") query: String?
    ): List<Person> {
        logger.debug("Find person with $query")
        val person = if (query != null) {
            val output = arrayListOf<Person>()
            output.addAll(analyzers.smartQuery(orgId, query))
            logger.debug("size smart query ${output.size}")
            if (output.size < per_page) {
                persons.find(query, orgId).forEach { person ->
                    if (output.find { person.id == it.id } == null) {
                        output.add(person)
                    }
                }
            }
            output
        } else {
            persons.findByOrgId(orgId)
        }
        return person.sortedWith(compareBy<Person> { it.name })
            .paging(page, per_page)
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE")
    @RolesAllowed("ADMIN", "PROVIDER")
    @Cache(maxAge = 5)
    fun getByPersonId(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String
    ): Person {
        return persons.getPerson(orgId, personId)
    }
}
