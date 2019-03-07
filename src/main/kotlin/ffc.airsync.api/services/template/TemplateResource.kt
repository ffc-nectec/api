package ffc.airsync.api.services.template

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.entity.Template
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
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
    fun find(
        @PathParam("orgId") orgId: String,
        @QueryParam("query") query: String?
    ): List<Template> {
        return templates.find(orgId, query ?: "")
    }

    @POST
    @Path("/$ORGIDTYPE/template")
    @RolesAllowed("ORG", "ADMIN")
    fun clearAndInsert(
        @PathParam("orgId") orgId: String,
        template: List<Template>
    ): List<Template> {
        templates.removeByOrgId(orgId)
        templates.insert(orgId, template)
        return template
    }
}
