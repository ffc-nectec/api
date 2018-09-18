package ffc.airsync.api.services

import ffc.airsync.api.services.filter.Cache
import ffc.airsync.api.services.module.DiseaseService
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
class DiseasesResource {
    @Context
    lateinit var req: HttpServletRequest

    @Cache(maxAge = 3600)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/disease")
    @GET
    fun query(@QueryParam("query") query: String?): List<Disease> {
        val disease = DiseaseService.query(query ?: "", req.locale.toLang())
        if (disease.isEmpty()) throw NotFoundException("ไม่พบข้อมูล")
        return disease
    }
}
