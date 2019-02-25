package ffc.airsync.api.services.image

import ffc.airsync.api.FFCApiServer
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.util.getUserLogin
import ffc.entity.Template
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.DefaultValue
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

@Path("/org")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ImageResource {
    @Context
    lateinit var context: SecurityContext
    val logger = getLogger()

    @POST
    @Path("/$ORGIDTYPE/image")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun postImage(
        @DefaultValue("true") @FormDataParam("enabled") enabled: Boolean,
        @FormDataParam("file") file: InputStream,
        @FormDataParam("file") fileDetail: FormDataContentDisposition,
        @PathParam("orgId") orgId: String
    ): Response {
        val reqBody = createRequestStream(file)
        val response = okRun("""${FFCApiServer.thumborUrl}/image""", reqBody)
        val fileLocation = Template("Location", response.header("Location") ?: "")
        logger.info("Create image by User:${context.getUserLogin()} Org:$orgId Method:postimage")
        return Response.status(200).entity(fileLocation).build()
    }
}
