package ffc.airsync.api.services.filter

import ffc.airsync.api.dao.DaoFactory
import ffc.airsync.api.printDebug
import ffc.entity.Token
import ffc.entity.User
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
        val urlScheme = requestContext.uriInfo.baseUri.scheme
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
            printDebug("Finish create TokenAuthInfo")
        } catch (ex: NotAuthorizedException) {
            return
        }


        val securityContext: SecurityContext


        securityContext = when {
            authenInfo.token.user.role == User.Role.USER ->
                UserSecurityContextImp(authenInfo.token, urlScheme, orgId)
            authenInfo.token.user.role == User.Role.ORG ->
                OrgSecurityContextImp(authenInfo.token, urlScheme, orgId)
            else ->
                NoAuthSecurityContextImp()
        }

        requestContext.securityContext = securityContext
    }


    class TokenAuthInfo(requestContext: ContainerRequestContext) {
        val AUTHORIZATION_PROPERTY = "Authorization"
        val AUTHENTICATION_SCHEME = "Bearer "
        val token: Token

        init {
            printDebug("TokenAuthInfo class in filter")
            val authorization = requestContext.headers[AUTHORIZATION_PROPERTY]

            if (authorization != null) {

                if (authorization[0].startsWith("Basic ")) {
                    throw NotAuthorizedException("is basic auth")
                }
                val tokenDao = DaoFactory().buildTokenMapDao()
                val tokenStr = authorization[0].replaceFirst(AUTHENTICATION_SCHEME, "").trim()
                printDebug("\tFind token.")
                token = tokenDao.find(token = tokenStr)
                printDebug("\t\ttoken = $token")

                if (token.isExpire) throw NotAuthorizedException("Token expire ${token.expireDate}")

            } else {
                token = Token(token = "", user = User())
            }

        }
    }


}



