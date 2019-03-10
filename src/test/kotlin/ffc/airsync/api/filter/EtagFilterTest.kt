package ffc.airsync.api.filter

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.`it returns`
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.mock
import org.junit.Test
import javax.ws.rs.container.ContainerRequestContext

import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.core.EntityTag
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Request
import javax.ws.rs.core.Response

class EtagFilterTest {

    @Test
    fun addEtagFor200Res() {
        val responseCtx = mock<ContainerResponseContext> {
            When calling it.status itReturns 200
            When calling it.hasEntity() itReturns true
            When calling it.entity itReturns "Hello Etag"
        }
        val resHeaders = mock<MultivaluedMap<String, Any>>()
        When calling responseCtx.headers itReturns resHeaders
        val requestCtx = mock<ContainerRequestContext>()
        val request = mock<Request>()
        When calling requestCtx.request itReturns request
        When calling request.evaluatePreconditions(any<EntityTag>()) `it returns` null

        EtagFilter().filter(requestCtx, responseCtx)

        val tag = Etag(responseCtx.entity)
        verify(resHeaders).add(eq("ETag"), eq("\"${tag.value}\""))
    }

    @Test
    fun emptyResponseFor304() {
        val responseCtx = mock<ContainerResponseContext> {
            When calling it.status itReturns 200
            When calling it.hasEntity() itReturns true
            When calling it.entity itReturns "Hello Etag"
        }
        val resHeaders = mock<MultivaluedMap<String, Any>>()
        When calling responseCtx.headers itReturns resHeaders
        val requestCtx = mock<ContainerRequestContext>()
        val request = mock<Request>()
        val resBuilder = mock<Response.ResponseBuilder>()
        When calling requestCtx.request itReturns request
        When calling request.evaluatePreconditions(any<EntityTag>()) `it returns` resBuilder

        EtagFilter().filter(requestCtx, responseCtx)

        verify(responseCtx).status = 304
        verify(responseCtx).entity = null
    }
}
