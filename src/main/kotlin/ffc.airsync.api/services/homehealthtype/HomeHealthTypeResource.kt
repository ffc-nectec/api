package ffc.airsync.api.services.homehealthtype

import ffc.airsync.api.filter.cache.Cache
import ffc.entity.healthcare.CommunityService.ServiceType
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.PathParam
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

    @GET
    @Path("/homehealth/{id:(.+)}")
    @Cache(maxAge = 3600)
    fun get(@PathParam("id") id: String): ServiceType {
        return homeHealthTypes.get(id) ?: throw NotFoundException("ไม่พบข้อมูล Health type id $id ที่ระบุ")
    }
}
