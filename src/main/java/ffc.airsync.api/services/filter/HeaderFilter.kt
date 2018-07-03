package ffc.airsync.api.services.filter

import javax.annotation.Priority
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

@Suppress("UNUSED_VARIABLE")
@Priority(Priorities.HEADER_DECORATOR)
@Provider

class HeaderFilter : ContainerResponseFilter {
    override fun filter(requestContext: ContainerRequestContext?, responseContext: ContainerResponseContext?) {
        val auth: String? = requestContext?.getHeaderString("Authorization")
    }
}
