package ffc.airsync.api.services

import ffc.airsync.api.services.module.EntityService
import ffc.entity.Entity
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/org")
class EntityResource {
    @RolesAllowed("USER", "ORG")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/sync")
    fun sync(@PathParam("orgId") orgId: String): List<Entity> {
        return EntityService.getNonSyncData(orgId)
    }
}
