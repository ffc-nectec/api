package ffc.airsync.api.filter.cache

import ffc.airsync.api.services.util.md5
import ffc.entity.gson.toJson
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.core.EntityTag
import javax.ws.rs.ext.Provider

@Provider
class EtagFilter : ContainerResponseFilter {

    override fun filter(request: ContainerRequestContext, response: ContainerResponseContext) {
        if (response.status != 200) return
        if (!response.hasEntity()) return

        val hash = Etag(response.entity).value
        val etag = EntityTag(hash)

        val responseBuilder = request.request.evaluatePreconditions(etag)
        if (responseBuilder != null) {
            response.status = 304
            response.entity = null
        }
        response.headers.add("ETag", "\"$hash\"")
    }
}

class Etag(content: Any) {

    val value: String = content.toJson().md5()
}
