package ffc.airsync.api.services

import ffc.airsync.api.filter.Cache
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class RootResource {

    @GET
    @Cache(maxAge = 3600)
    fun getRoot(): Map<String, Any> {
        return mapOf(
            "name" to "FFC-API",
            "websiteUrl" to "https://www.ffc.in.th",
            "vcsUrl" to "https://github.com/ffc-nectec/api.git",
            "issueTrackerUrl" to "https://github.com/ffc-nectec/api/issues",
            "releaseUrl" to "https://github.com/ffc-nectec/api/releases",
            "copyright" to "NECTEC",
            "license" to "Apache License 2.0",
            "licenseUrl" to "http://www.apache.org/licenses/",
            "developers" to arrayOf(
                Contributor("Thanachai Thongkum", "https://github.com/lionants02"),
                Contributor("Piruin Panichphol", "https://github.com/piruin"),
                Contributor("Porntipa Choksungnoen", "https://github.com/porntipa")
            )
        )
    }

    class Contributor(val name: String, val url: String)
}
