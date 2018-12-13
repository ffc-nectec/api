package ffc.airsync.api.services.genogram

import ffc.airsync.api.filter.Cache
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

    @POST
    @Path("/$ORGIDTYPE/person/relationship/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 5)
    fun insertBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int,
        relation: Map<String, @JvmSuppressWildcards List<Person.Relationship>>
    ): Map<String, List<Person.Relationship>> {
        try {
            return personRelationsShip.insertBlock(orgId, block, relation)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    @GET
    @Path("/$ORGIDTYPE/person/relationship/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 5)
    fun getBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ): Map<String, List<Person.Relationship>> {
        try {
            return personRelationsShip.getBlock(orgId, block)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    @PUT
    @Path("/$ORGIDTYPE/person/relationship/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 5)
    fun confirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        try {
            personRelationsShip.confirmBlock(orgId, block)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/relationship/sync/$BLOCKTYPE")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 5)
    fun unConfirmBlock(
        @PathParam("orgId") orgId: String,
        @PathParam("block") block: Int
    ) {
        try {
            personRelationsShip.unConfirmBlock(orgId, block)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    @DELETE
    @Path("/$ORGIDTYPE/person/relationship/sync/clean")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 5)
    fun cleanAll(
        @PathParam("orgId") orgId: String
    ) {
        try {
            personRelationsShip.removeByOrgId(orgId)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    @DELETE
    @Path("/relationship/insertblock")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 5)
    fun removeInsert() {
        try {
            personRelationsShip.removeInsertBlock()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }
}
