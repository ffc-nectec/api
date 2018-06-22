package ffc.airsync.api.services.filter

import ffc.airsync.api.dao.DaoFactory
import ffc.airsync.api.printDebug
import ffc.entity.StorageOrg
import ffc.entity.TokenMessage
import java.util.*
import java.util.regex.Pattern
import javax.annotation.Priority
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.ext.Provider

@Priority(Priorities.AUTHENTICATION)
@Provider
class BasicAuthFilter : ContainerRequestFilter {
    private val pattern = Pattern.compile("""^org/(?<orgId>[\w\d]+)/.*$""")


    override fun filter(requestContext: ContainerRequestContext) {
        val urlScheme = requestContext.getUriInfo().getBaseUri().getScheme()
        val baseUrl = requestContext.uriInfo.path.toString()


        val matcher = pattern.matcher(baseUrl)

        var orgId: String = ""

        if (matcher.find()) {
            orgId = matcher.group(1)
        }
        printDebug("Auth filter parth url $baseUrl")
        printDebug("\t Org id = $orgId")


        val authenInfo: TokenAuthInfo

        try {
            authenInfo = TokenAuthInfo(requestContext)
        } catch (ex: NotAuthorizedException) {
            return
        }


        val securityContext: SecurityContext


        if (authenInfo.token.data.role == TokenMessage.TYPEROLE.USER) {
            //if(authenInfo.token.id==orgId)
            securityContext = UserSecurityContextImp(authenInfo.token.data, urlScheme, orgId)
        } else if (authenInfo.token.data.role == TokenMessage.TYPEROLE.ORG) {
            securityContext = OrgSecurityContextImp(authenInfo.token.data, urlScheme, orgId)
        } else {
            securityContext = NoAuthSecurityContextImp()
        }

        requestContext.setSecurityContext(securityContext)
    }


    class TokenAuthInfo(requestContext: ContainerRequestContext) {
        val AUTHORIZATION_PROPERTY = "Authorization"
        val AUTHENTICATION_SCHEME = "Bearer "
        val token: StorageOrg<TokenMessage>

        init {
            val authorization = requestContext.headers[AUTHORIZATION_PROPERTY]

            if (authorization != null) {

                if (authorization[0].startsWith("Basic ")) {
                    throw NotAuthorizedException("is basic auth")
                }
                val tokenMobile = DaoFactory().buildTokenMobileMapDao()
                val tokenStr = authorization[0].replaceFirst(AUTHENTICATION_SCHEME, "").trim()
                token = tokenMobile.find(token = UUID.fromString(tokenStr))

                if (token.data.isExpire) throw NotAuthorizedException("Token expire ${token.data.expireDate}")

            } else {
                token = StorageOrg(UUID.randomUUID(), TokenMessage(UUID.randomUUID(), name = "NOAUTH"))
            }

        }
    }


}



