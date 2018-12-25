package ffc.airsync.api.services.template

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Template
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TemplateResource {

    @GET
    @Path("/$ORGIDTYPE/template")
    @Cache(maxAge = 3600)
    fun find(@QueryParam("query") query: String?): List<Template> {
        return templates.find(query ?: "")
    }
}
