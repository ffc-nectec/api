package ffc.airsync.api.filter

import java.lang.IllegalArgumentException
import java.util.ArrayList
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
        var cache: Cache? = resourceInfo.resourceMethod.getAnnotation(Cache::class.java)
        if (cache == null) cache = resourceInfo.resourceClass.getAnnotation(Cache::class.java)
        if (cache != null) responseContext.headers.add("Cache-Control", build(cache, requestContext.cacheControl))
    }

    private fun isCacheable(responseContext: ContainerResponseContext): Boolean {
        val status = responseContext.status
        return status in 200..299 || status == 304
    }

    private fun build(cache: Cache, reqCache: CacheControl): String {
        if (cache.noStore || reqCache.isNoStore) {
            return "no-store"
        }

        val cacheOption = ArrayList<String>()
        if (cache.noCache || reqCache.isNoCache) cacheOption.add("no-cache")
        if (cache.mustRevalidate || reqCache.isMustRevalidate) cacheOption.add("must-revalidate")
        if (cache.isPrivate || reqCache.isPrivate) cacheOption.add("private")
        if (cache.maxAge > -1) {
            val maxAge = reqCache.maxAge.takeIf { -1 < it && it < cache.maxAge } ?: cache.maxAge
            cacheOption.add("max-age=$maxAge")
        }

        return cacheOption.joinToString(", ")
    }
}

val ContainerRequestContext.cacheControl: CacheControl
    get() {
        val header = headers["Cache-Control"]
        return try {
            if (header != null) {
                CacheControl.valueOf(header.first())
            } else {
                CacheControl()
            }
        } catch (illegal : IllegalArgumentException) {
            CacheControl()
        }
    }
