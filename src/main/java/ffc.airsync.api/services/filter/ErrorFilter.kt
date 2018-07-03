package ffc.airsync.api.services.filter

import ffc.airsync.api.printDebug
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ErrorFilter : ExceptionMapper<WebApplicationException> {

    override fun toResponse(exception: WebApplicationException?): Response {
        printDebug("Api wrong")
        exception!!.printStackTrace()
        val err = ErrorRes(exception.response.status, exception.message, exception)
        return Response.status(exception.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }

    data class ErrorRes(val code: Int, val message: String?, val t: Throwable)
}

@Provider
class ErrorUserFilter : ExceptionMapper<ForbiddenException> {
    override fun toResponse(exception: ForbiddenException?): Response {
        exception!!.printStackTrace()
        var err = ErrorFilter.ErrorRes(exception.response.status, exception.message, exception)
        return if (exception.message == "User not authorized.") {
            val except = NotAuthorizedException("token not found")
            err = ErrorFilter.ErrorRes(401, except.message, except)
            Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
        } else {
            Response.status(exception.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
        }
    }
}
