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

package ffc.airsync.api.services.person

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.printDebug
import ffc.airsync.api.services.util.paging
import ffc.entity.Person
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
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
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class PersonResource {
    @Context
    private var context: SecurityContext? = null

    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/persons")
    @RolesAllowed("ORG", "ADMIN")
    fun creates(@PathParam("orgId") orgId: String, personList: List<Person>): Response {
        printDebug("\nCall create person by ip = ")
        val persons = persons.insert(orgId, personList)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person")
    @RolesAllowed("ORG", "ADMIN")
    fun create(@PathParam("orgId") orgId: String, person: Person): Response {
        printDebug("\nCall create person by ip = ")
        val persons = persons.insert(orgId, person)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun get(@QueryParam("page") page: Int = 1, @QueryParam("per_page") per_page: Int = 200, @PathParam("orgId") orgId: String, @QueryParam("query") query: String?): List<Person> {
        return if (query != null) {
            persons.find(query, orgId)
        } else {
            persons.findByOrgId(orgId).paging(if (page == 0) 1 else page, if (per_page == 0) 200 else per_page)
        }
    }

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/{personId:([\\dabcdefABCDEF].*)}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun getByPersonId(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): Person {
        return persons.getPerson(orgId, personId)
    }

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/icd10/{icd10:(\\w+)}")
    @RolesAllowed("USER")
    fun findByICD10(@PathParam("orgId") orgId: String, @PathParam("icd10") icd10: String): List<Person> {
        return persons.findByICD10(orgId, icd10)
    }
}
