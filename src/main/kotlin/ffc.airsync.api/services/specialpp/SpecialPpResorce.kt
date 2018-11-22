package ffc.airsync.api.services.specialpp

import ffc.airsync.api.filter.Cache
import ffc.entity.healthcare.SpecialPP
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
class SpecialPpResorce {

    @Context
    lateinit var req: HttpServletRequest

    @GET
    @Path("/specialPP")
    @Cache(maxAge = 3600)
    fun query(@QueryParam("query") query: String?): List<SpecialPP.PPType> {
        return specialPPs.query(query ?: "")
    }

    @GET
    @Path("/specialPP/{specialId:(.+)}")
    @Cache(maxAge = 3600)
    fun get(@PathParam("specialId") id: String): SpecialPP.PPType {
        return specialPPs.get(id)
    }
}
