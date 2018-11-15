package ffc.airsync.api.filter

import ffc.airsync.api.printDebug
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.NotAllowedException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ErrorFilter : ExceptionMapper<WebApplicationException> {
    override fun toResponse(exception: WebApplicationException): Response {
        printDebug("Api wrong")
        // exception!!.printStackTrace()
        val err = ErrorRes(exception.response.status, exception.message, exception)
        return Response.status(exception.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }

    data class ErrorRes(val code: Int, val message: String?, val t: Throwable)
}

@Provider
class ErrorUserFilter : ExceptionMapper<ForbiddenException> {
    override fun toResponse(exception: ForbiddenException): Response {
        // exception!!.printStackTrace()
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

@Provider
class ErrorMethodNotAllow : ExceptionMapper<javax.ws.rs.NotAllowedException> {
    override fun toResponse(exception: javax.ws.rs.NotAllowedException): Response {
        // exception!!.printStackTrace()
        val except = BadRequestException(exception.message)
        val err = ErrorFilter.ErrorRes(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class RequireError : ExceptionMapper<IllegalArgumentException> {
    override fun toResponse(exception: IllegalArgumentException): Response {
        // exception!!.printStackTrace()
        if ((exception.message ?: "").endsWith("parameter houseId")) {
            val except = NotAllowedException(exception.message)
            val err = ErrorFilter.ErrorRes(except.response.status, exception.message, exception)
            return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
        }
        val except = BadRequestException(exception.message)
        val err = ErrorFilter.ErrorRes(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class StaegError : ExceptionMapper<IllegalStateException> {
    override fun toResponse(exception: IllegalStateException): Response {
        // exception!!.printStackTrace()
        val except = InternalServerErrorException("Stage error ${exception.message}")
        val err = ErrorFilter.ErrorRes(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class NullError : ExceptionMapper<NullPointerException> {
    override fun toResponse(exception: NullPointerException): Response {
        // exception!!.printStackTrace()
        val except = NotFoundException("Null error ${exception.message}")
        val err = ErrorFilter.ErrorRes(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class NotSuchElement : ExceptionMapper<NoSuchElementException> {
    override fun toResponse(exception: NoSuchElementException): Response {
        // exception!!.printStackTrace()
        val except = NotFoundException("No Such Element ${exception.message}")
        val err = ErrorFilter.ErrorRes(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}
