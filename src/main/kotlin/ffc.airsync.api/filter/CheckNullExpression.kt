package ffc.airsync.api.filter

import javax.annotation.Priority
import javax.ws.rs.NotFoundException
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

@Suppress("UNUSED_VARIABLE")
@Priority(Priorities.HEADER_DECORATOR)
@Provider
class CheckNullExpression : ContainerResponseFilter {
    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext?) {
        if (responseContext == null) throw NotFoundException()
        if (responseContext.entity == null) throw NotFoundException()
        if (responseContext.entity is Exception) throw responseContext.entity as Exception
    }
}
