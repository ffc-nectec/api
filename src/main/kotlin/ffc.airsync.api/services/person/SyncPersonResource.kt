package ffc.airsync.api.services.person

import ffc.airsync.api.services.BLOCKTYPE
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Person
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.OPTIONS
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SyncPersonResource {

    @Context
    private var context: SecurityContext? = null

    @POST
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun creates(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        personList: List<Person>
    ): Response {
        personList.forEach { person ->
            mapDeadIcd10(person)
        }
        val persons = persons.insertBlock(orgId, block, personList)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }

    @OPTIONS
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun confirm(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Response {
        persons.confirmBlock(orgId, block)
        return Response.status(Response.Status.CREATED).build()
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun unConfirm(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Response {
        persons.unConfirmBlock(orgId, block)
        return Response.status(Response.Status.CREATED).build()
    }

    @GET
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    fun get(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Response {
        val persons = persons.getBlock(orgId, block)
        return Response.status(Response.Status.CREATED).entity(persons).build()
    }
}
