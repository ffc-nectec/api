package ffc.airsync.api.services.disease

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.toLang
import ffc.entity.healthcare.Disease
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
class DiseasesResource {

    @Context
    lateinit var req: HttpServletRequest

    @GET
    @Path("/disease")
    @Cache(maxAge = 3600)
    fun query(@QueryParam("query") query: String?): List<Disease> {
        val disease = diseases.find(query ?: "", req.locale.toLang())
        if (disease.isEmpty()) throw NotFoundException("ไม่พบข้อมูล")
        return disease
    }
}
