package ffc.airsync.api.security

import ffc.airsync.api.printDebug
import java.util.regex.Pattern
import javax.annotation.Priority
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

@Priority(Priorities.AUTHENTICATION)
@Provider
class BasicAuthFilter : ContainerRequestFilter {
    private val pattern = Pattern.compile("""^org/(?<orgId>[\w\d]+)/.*$""")
    override fun filter(requestContext: ContainerRequestContext) {
        val urlScheme = requestContext.uriInfo.baseUri.scheme
        val baseUrl = requestContext.uriInfo.path.toString()
        val matcherOrgId = pattern.matcher(baseUrl)
        var orgId = ""

        if (matcherOrgId.find()) {
            orgId = matcherOrgId.group(1)
        }
        printDebug("Auth filter parth url $baseUrl \t Org id = $orgId")
        val authenInfo: BasicTokenInfo

        try {
            authenInfo = BasicTokenInfo(requestContext)
            printDebug("Finish create TokenInfo")
        } catch (ex: NotAuthorizedException) {
            return
        }
        val token = authenInfo.token

        requestContext.securityContext = WebRoleContext(
            token = token,
            scheme = urlScheme,
            orgId = orgId
        )
    }
}
