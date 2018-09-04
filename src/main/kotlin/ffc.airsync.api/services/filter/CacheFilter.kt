package ffc.airsync.api.services.filter

import java.util.ArrayList
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.ext.Provider

@Provider
class CacheFilter : ContainerResponseFilter {

    @Context
    private lateinit var resourceInfo: ResourceInfo

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        if (!isCacheable(responseContext)) {
            responseContext.headers.add("Cache-Control", "max-age=0")
            return
        }

        var cache: Cache? = resourceInfo!!.resourceMethod.getAnnotation(Cache::class.java)
        if (cache == null) cache = resourceInfo.resourceClass.getAnnotation(Cache::class.java)
        if (cache != null) responseContext.headers.add("Cache-Control", build(cache))
    }

    private fun isCacheable(responseContext: ContainerResponseContext): Boolean {
        val status = responseContext.status
        return status >= 200 && status < 300 || status == 304
    }

    private fun build(cache: Cache): String {
        if (cache.noStore) {
            return "no-store"
        }

        val cacheOption = ArrayList<String>()
        if (cache.noCache) cacheOption.add("no-cache")
        if (cache.isPrivate) cacheOption.add("private")
        if (cache.maxAge > -1) cacheOption.add("max-age=" + cache.maxAge)

        return cacheOption.joinToString(", ")
    }
}
