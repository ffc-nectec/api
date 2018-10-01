package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.filter.Cache
import ffc.entity.healthcare.CommunityServiceType
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/")
class HomeHealthTypeResource {
    @Cache(maxAge = 3600)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/homehealth")
    @GET
    fun query(@QueryParam("query") query: String?): List<CommunityServiceType> {
        val homeHealthType = HomeHealthTypeService.query(query ?: "")
        if (homeHealthType.isEmpty()) throw NotFoundException("ไม่พบข้อมูล")
        return homeHealthType
    }
}
