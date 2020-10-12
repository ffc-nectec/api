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
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.PERSONIDTYPE
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.genogram.Family
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext
import kotlin.math.absoluteValue

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

class RelationshipResource {
    @Context
    private var context: SecurityContext? = null

    @GET
    @Path("/$ORGIDTYPE/person/{personId:([\\dabcdefABCDEF]+)}/relationship")
    @RolesAllowed("ADMIN", "PROVIDER", "PATIENT")
    @Cache(maxAge = 5)
    fun get(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): List<Person.Relationship> {
        return personRelationsShip.get(orgId, personId)
    }

    @PUT
    @Path("/$ORGIDTYPE/person/{personId:([\\dabcdefABCDEF]+)}/relationship")
    @RolesAllowed("ADMIN")
    @Cache(maxAge = 5)
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("personId") personId: String,
        relationship: List<Person.Relationship>
    ): List<Person.Relationship> {
        return personRelationsShip.update(orgId, personId, relationship)
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/genogram/collect")
    @RolesAllowed("ADMIN", "PROVIDER", "PATIENT")
    @Cache(maxAge = 5)
    fun getGenogramCollect(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): List<Person> {
        return personRelationsShip.collectGenogram(orgId, personId)
    }

    @GET
    @Path("/$ORGIDTYPE/person/$PERSONIDTYPE/genogram")
    @RolesAllowed("ADMIN", "PROVIDER", "PATIENT")
    @Cache(maxAge = 5)
    fun getGenogramFamily(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): Family {

        val collect = getGenogramCollect(orgId, personId).processGroupLayer().toList()

        val member: List<ffc.genogram.Person> = collect.map {
            it.buildGeogramPerson(collect)
        }
        val somePerson = collect.first()

        return Family(somePerson.houseId.hashCode().toLong().absoluteValue, somePerson.lastname, member)
    }

    @GET
    @Path("/person/genogram/demo")
    @Cache(maxAge = 5)
    fun demo2(): Family {
        return demo("11")
    }

    @GET
    @Path("/$ORGIDTYPE/person/genogram/demo")
    @Cache(maxAge = 5)
    fun demo(@PathParam("orgId") orgId: String): Family {
        val json = """
            {
  "familyId": 2,
  "familyName": "Smiths",
  "bloodFamily": [
    0,
    1,
    2,
    3,
    4,
    5
  ],
  "members": [
    {
      "idCard": 0,
      "firstname": "Grandfather",
      "lastname": "Smiths",
      "birthDate": "1-1-1970",
      "gender": 0,
      "father": null,
      "mother": null,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": null,
      "wife": [
        10
      ],
      "children": [
        1,
        2
      ],
      "linkedStack": [
        10,
        1,
        2
      ]
    },
    {
      "idCard": 10,
      "firstname": "Grandmother",
      "lastname": "Smiths",
      "birthDate": "12-11-1972",
      "gender": 1,
      "father": null,
      "mother": null,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": [
        0
      ],
      "wife": null,
      "children": [
        1,
        2
      ],
      "linkedStack": [
        0,
        1,
        2
      ]
    },
    {
      "idCard": 1,
      "firstname": "Lisa",
      "lastname": "Snow",
      "birthDate": "12-11-1980",
      "gender": 1,
      "father": 0,
      "mother": 10,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": [
        13
      ],
      "wife": null,
      "children": [
        3,
        4,
        5
      ],
      "linkedStack": [
        0,
        10,
        13,
        3,
        4,
        5
      ]
    },
    {
      "idCard": 13,
      "firstname": "Bill",
      "lastname": "Snow",
      "birthDate": "12-11-1980",
      "gender": 0,
      "father": null,
      "mother": null,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": null,
      "wife": [
        1
      ],
      "children": [
        3,
        4,
        5
      ],
      "linkedStack": [
        1,
        3,
        4,
        5
      ]
    },
    {
      "idCard": 3,
      "firstname": "River",
      "lastname": "Snow",
      "birthDate": "12-11-2001",
      "gender": 0,
      "father": 13,
      "mother": 1,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": null,
      "wife": null,
      "children": null,
      "linkedStack": [
        13,
        1
      ]
    },
    {
      "idCard": 4,
      "firstname": "Will",
      "lastname": "Snow",
      "birthDate": "12-11-2003",
      "gender": 0,
      "father": 13,
      "mother": 1,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": null,
      "wife": null,
      "children": null,
      "linkedStack": [
        13,
        1
      ]
    },
    {
      "idCard": 5,
      "firstname": "Sarah",
      "lastname": "Snow",
      "birthDate": "12-11-2004",
      "gender": 1,
      "father": 13,
      "mother": 1,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": null,
      "wife": null,
      "children": null,
      "linkedStack": [
        13,
        1
      ]
    },
    {
      "idCard": 2,
      "firstname": "Ed",
      "lastname": "Smiths",
      "birthDate": "23-10-1982",
      "gender": 0,
      "father": 0,
      "mother": 10,
      "twin": null,
      "ex-husband": null,
      "ex-wife": null,
      "husband": null,
      "wife": null,
      "children": null,
      "linkedStack": [
        0,
        10
      ]
    }
  ]
}
        """.trimIndent()
        return json.parseTo()
    }
}
