package ffc.airsync.api.filter

import javax.annotation.Priority
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.ext.Provider

@Priority(Priorities.HEADER_DECORATOR)
@Provider
class DeveloperFilter : ContainerResponseFilter {

    @Context
    private lateinit var resourceInfo: ResourceInfo

    override fun filter(requestContext: ContainerRequestContext?, responseContext: ContainerResponseContext) {

        if ((resourceInfo.resourceMethod.getAnnotation(Developer::class.java)) != null) {
            responseContext.headers.add("access-control-allow-credentials", "true")
            responseContext.headers.add("access-control-allow-origin", "*")
            responseContext.headers.add("Access-Control-Allow-Headers", "X-Custom-Header, Upgrade-Insecure-Requests")
        }
    }
}
