package ffc.airsync.api.filter

import javax.annotation.Priority
import javax.ws.rs.NotFoundException
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

@Suppress("UNUSED_VARIABLE")
@Priority(Priorities.ENTITY_CODER)
@Provider
class EmptyListResponse : ContainerResponseFilter {
    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        val list = responseContext.entity as? List<*> ?: return
        if (list.isEmpty()) throw NotFoundException("Empty List")
    }
}
