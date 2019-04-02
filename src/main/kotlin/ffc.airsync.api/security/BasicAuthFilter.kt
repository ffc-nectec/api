package ffc.airsync.api.security

import ffc.airsync.api.getLoggerC
import ffc.airsync.api.services.util.getLoginRole
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
    private val logger by lazy { getLoggerC(this) }
    override fun filter(requestContext: ContainerRequestContext) {
        val urlScheme = requestContext.uriInfo.baseUri.scheme
        val baseUrl = requestContext.uriInfo.path.toString()
        val matcherOrgId = pattern.matcher(baseUrl)
        var orgId = ""

        if (matcherOrgId.find()) {
            orgId = matcherOrgId.group(1)
        }
        val authenInfo: BasicTokenInfo

        try {
            authenInfo = BasicTokenInfo(requestContext, orgId)
        } catch (ex: NotAuthorizedException) {
            logger.warn("Token fail", ex)
            return
        }
        val token = authenInfo.token

        requestContext.securityContext = WebRoleContext(
            token = token,
            scheme = urlScheme,
            orgId = orgId
        )

        val name = requestContext.securityContext.userPrincipal.name
        val loginRole = requestContext.securityContext.getLoginRole()
        val httpMethod = requestContext.method
        logger.info("Basic auth log User:$name Role:$loginRole Org:$orgId Method:$httpMethod Url:$baseUrl")
    }
}
