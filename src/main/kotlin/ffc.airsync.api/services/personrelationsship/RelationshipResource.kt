package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.filter.Cache
import ffc.entity.Person
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

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class RelationshipResource {
    @Context
    private var context: SecurityContext? = null

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/{personId:([\\dabcdefABCDEF].*)}/relationship")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun get(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): List<Person.Relationship> {
        return personRelationsShip.get(orgId, personId)
    }

    @PUT
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/{personId:([\\dabcdefABCDEF].*)}/relationship")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun update(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String, relationship: List<Person.Relationship>): List<Person.Relationship> {
        return personRelationsShip.update(orgId, personId, relationship)
    }

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/person/{personId:([\\dabcdefABCDEF].*)}/genogram/collect")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Cache(maxAge = 5)
    fun getGenogram(@PathParam("orgId") orgId: String, @PathParam("personId") personId: String): List<Person> {
        return personRelationsShip.collectGenogram(orgId, personId)
    }
}
