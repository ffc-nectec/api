package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.filter.Cache
import ffc.entity.healthcare.CommunityService.ServiceType
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
class HomeHealthTypeResource {

    @GET
    @Path("/homehealth")
    @Cache(maxAge = 3600)
    fun query(@QueryParam("query") query: String?): List<ServiceType> {
        return HomeHealthTypeService.query(query ?: "")
    }
}
