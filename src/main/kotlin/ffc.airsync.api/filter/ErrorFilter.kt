/*
 * Copyright (c) 2019 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ffc.airsync.api.filter

import com.mongodb.MongoException
import ffc.airsync.api.getLogger
import ffc.airsync.api.logLevel
import org.apache.logging.log4j.Level
import javax.ws.rs.BadRequestException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.NotAllowedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

class ErrorDetail(
    val code: Int,
    val message: String?,
    t: Throwable
) {
    val tType = t::class.java.simpleName
    private var t: Throwable? = null

    init {
        if (logLevel != Level.INFO) {
            this.t = t
        }
        getLogger().debug("${t.message}", t)
    }
}

@Provider
class ErrorFilter : ExceptionMapper<WebApplicationException> {
    override fun toResponse(exception: WebApplicationException): Response {
        if (exception.response.status in 400..499) {
            getLogger().info("Client Error", exception)
        } else {
            getLogger().error("Server Error", exception)
        }
        val err = ErrorDetail(exception.response.status, exception.message, exception)
        return Response.status(exception.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class MongoExceptionFilter : ExceptionMapper<MongoException> {
    override fun toResponse(exception: MongoException): Response {
        getLogger().error("MongoClient Error", exception)
        val err = ErrorDetail(500, exception.message, exception)
        return Response.status(500).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class ErrorMethodNotAllow : ExceptionMapper<NotAllowedException> {
    override fun toResponse(exception: NotAllowedException): Response {
        val except = BadRequestException(exception.message)
        val err = ErrorDetail(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class RequireError : ExceptionMapper<IllegalArgumentException> {
    override fun toResponse(exception: IllegalArgumentException): Response {
        if ((exception.message ?: "").endsWith("parameter houseId")) {
            val except = NotAllowedException(exception.message)
            val err = ErrorDetail(except.response.status, exception.message, exception)
            return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
        }
        val except = BadRequestException(exception.message)
        val err = ErrorDetail(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class StaegError : ExceptionMapper<IllegalStateException> {
    override fun toResponse(exception: IllegalStateException): Response {
        val except = InternalServerErrorException("Stage error ${exception.message}")
        val err = ErrorDetail(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class NullError : ExceptionMapper<NullPointerException> {
    override fun toResponse(exception: NullPointerException): Response {
        val except = NotFoundException("Null error ${exception.message}")
        val err = ErrorDetail(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class NotSuchElement : ExceptionMapper<NoSuchElementException> {
    override fun toResponse(exception: NoSuchElementException): Response {
        val except = NotFoundException("No Such Element ${exception.message}")
        val err = ErrorDetail(except.response.status, exception.message, exception)
        return Response.status(except.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class CloudInternalServerErrorException : ExceptionMapper<InternalServerErrorException> {
    override fun toResponse(exception: InternalServerErrorException): Response {
        val err = ErrorDetail(exception.response.status, exception.message, exception)
        return Response.status(exception.response.statusInfo).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class UninitializedPropertyAccessExceptionFilter : ExceptionMapper<UninitializedPropertyAccessException> {
    override fun toResponse(exception: UninitializedPropertyAccessException): Response {
        getLogger().error("lateinit error", exception)
        val err = ErrorDetail(500, exception.message, exception)
        return Response.status(500).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class NoClassDefFoundErrorFilter : ExceptionMapper<NoClassDefFoundError> {
    override fun toResponse(exception: NoClassDefFoundError): Response {
        getLogger().error(exception)
        val err = ErrorDetail(500, exception.message, exception)
        return Response.status(500).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}

@Provider
class IllegalStateExceptionErrorFilter : ExceptionMapper<IllegalStateException> {
    override fun toResponse(exception: IllegalStateException): Response {
        getLogger().error(exception)
        val err = ErrorDetail(500, exception.message, exception)
        return Response.status(500).entity(err).type(MediaType.APPLICATION_JSON_TYPE).build()
    }
}
