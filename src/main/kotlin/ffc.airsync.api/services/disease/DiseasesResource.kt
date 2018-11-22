package ffc.airsync.api.services.disease

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.toLang
import ffc.entity.healthcare.Disease
import ffc.entity.healthcare.Icd10
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.PathParam
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
        return diseases.find(query ?: "", req.locale.toLang())
    }

    @GET
    @Path("/disease/icd10/{icd10:(.+)}")
    @Cache(maxAge = 3600)
    fun icd10(@PathParam("icd10") icd10: String): Icd10 {
        return diseases.getByIcd10(icd10) ?: throw NotFoundException("ไม่พบ icd10 $icd10 ที่ระบุ")
    }
}
