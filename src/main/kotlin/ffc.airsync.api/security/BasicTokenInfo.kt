package ffc.airsync.api.security

import ffc.airsync.api.getLoggerC
import ffc.airsync.api.services.token.tokens
import ffc.entity.Token
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.container.ContainerRequestContext

class BasicTokenInfo(requestContext: ContainerRequestContext, orgId: String) {
    val AUTHORIZATION_PROPERTY = "Authorization"
    val AUTHENTICATION_SCHEME = "Bearer "
    val token: Token
    private val logger by lazy { getLoggerC(this) }

    init {
        val authorization = requestContext.headers[AUTHORIZATION_PROPERTY]
        logger.debug(
            "OrgId=$orgId " +
                "Header=${requestContext.headers}"
        )
        if (authorization != null) {
            token = findToken(getBasicToken(authorization), orgId)
            checkTokenExpire()
        } else {
            throw NotAuthorizedException("โปรด Login เพื่อขอ Token")
        }
    }

    private fun checkTokenExpire() {
        if (token.isExpire) throw NotAuthorizedException("Token expire ${token.expireDate}")
    }

    private fun findToken(tokenStr: String, orgId: String) =
        tokens.login(token = tokenStr, orgId = orgId) ?: throw NotAuthorizedException("โปรด Login เพื่อขอ Token")

    private fun getBasicToken(authorization: List<String>): String {
        if (authorization[0].startsWith("Basic ")) {
            throw NotAuthorizedException("is basic auth")
        }
        return authorization[0].replaceFirst(AUTHENTICATION_SCHEME, "").trim()
    }
}
