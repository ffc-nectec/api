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

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.filter.Cache
import ffc.entity.Person
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/org")
class PersonResource {
    @Context
    private var context: SecurityContext? = null

    @RolesAllowed("ORG", "ADMIN")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/persons")
    fun creates(@PathParam("orgId") orgId: String, personList: List<Person>): Response {
        printDebug("\nCall create person by ip = ")
        val persons = PersonService.create(orgId, personList)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    @RolesAllowed("ORG", "ADMIN")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person")
    fun create(@PathParam("orgId") orgId: String, person: Person): Response {
        printDebug("\nCall create person by ip = ")
        val persons = PersonService.create(orgId, person)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    @Cache(maxAge = 5)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person")
    fun get(@QueryParam("page") page: Int = 1, @QueryParam("per_page") per_page: Int = 200, @PathParam("orgId") orgId: String, @QueryParam("query") query: String?): Response {
        return try {
            if (query != null) {
                val personList = PersonService.find(orgId, query)
                Response.status(Response.Status.OK).entity(personList).build()
            } else {
                val personList = PersonService.get(orgId, if (page == 0) 1 else page, if (per_page == 0) 200 else per_page)
                Response.status(Response.Status.OK).entity(personList).build()
            }
        } catch (ex: NotAuthorizedException) {
            Response.status(401).build()
        }
    }

    @Cache(maxAge = 5)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/{personId:([\\dabcdefABCDEF].*)}")
    fun getByPersonId(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): Person {
        return PersonService.getByPersonId(orgId, personId)
    }

    @RolesAllowed("USER")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/icd10/{icd10:(\\w+)}")
    fun findByICD10(@PathParam("orgId") orgId: String, @PathParam("icd10") icd10: String): List<Person> {
        return PersonService.findICD10(orgId, icd10)
    }
}