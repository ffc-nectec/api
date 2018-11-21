package ffc.airsync.api.services.person

import ffc.airsync.api.services.BLOCKTYPE
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.Sync
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
class SyncPersonResource : Sync<Person> {

    @Context
    private var context: SecurityContext? = null

    @POST
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    override fun insertBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        item: List<Person>
    ): List<Person> {
        item.forEach { person ->
            mapDeadIcd10(person)
        }
        return persons.insertBlock(orgId, block, item)
    }

    @PUT
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    override fun confirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        persons.confirmBlock(orgId, block)
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    override fun unConfirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        persons.unConfirmBlock(orgId, block)
    }

    @GET
    @Path("/$ORGIDTYPE/person/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    override fun getBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): List<Person> {
        return persons.getBlock(orgId, block)
    }
}
