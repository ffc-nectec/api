package ffc.airsync.api.filter.cache

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.CacheControl
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
        val cache: Cache? = resourceInfo.resourceMethod.getAnnotation(Cache::class.java)
            ?: resourceInfo.resourceClass.getAnnotation(Cache::class.java)

        if (cache != null)
            responseContext.headers.add("Cache-Control",
                cache.toCacheControl().combineWith(requestContext.cacheControl).toString())
    }

    private fun isCacheable(responseContext: ContainerResponseContext): Boolean {
        val status = responseContext.status
        return status in 200..299 || status == 304
    }
}

private val ContainerRequestContext.cacheControl: CacheControl
    get() {
        val header = headers["Cache-Control"]
        return try {
            if (header != null) {
                CacheControl.valueOf(header.first())
            } else {
                CacheControl().apply { isNoTransform = false }
            }
        } catch (illegal: IllegalArgumentException) {
            CacheControl().apply { isNoTransform = false }
        }
    }
