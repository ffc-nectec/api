package ffc.airsync.api.services

import ffc.airsync.api.services.module.DiseaseService
import ffc.entity.healthcare.Disease
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/")
class DiseasesResource {

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/disease")
    @GET
    fun query(@QueryParam("query") query: String): List<Disease> {
        return DiseaseService.query(query)
    }
}