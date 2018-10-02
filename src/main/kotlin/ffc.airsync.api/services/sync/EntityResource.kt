package ffc.airsync.api.services.sync

import ffc.airsync.api.filter.Cache
import ffc.entity.Entity
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class EntityResource {

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/sync")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 2)
    fun sync(@PathParam("orgId") orgId: String): List<Entity> {
        return EntityService.getNonSyncData(orgId)
    }

    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/sync/healthcareservice")
    @RolesAllowed("ORG", "ADMIN")
    @Cache(maxAge = 2)
    fun syncHealthCareService(@PathParam("orgId") orgId: String): List<Entity> {
        return EntityService.getHealthCareService(orgId)
    }
}
