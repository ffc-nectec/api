package ffc.airsync.api.security

import ffc.airsync.api.services.token.tokens
import ffc.entity.Token
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.container.ContainerRequestContext

class BasicTokenInfo(requestContext: ContainerRequestContext) {
    val AUTHORIZATION_PROPERTY = "Authorization"
    val AUTHENTICATION_SCHEME = "Bearer "
    val token: Token

    init {
        val authorization = requestContext.headers[AUTHORIZATION_PROPERTY]

        if (authorization != null) {
            try {
                token = findToken(getBasicToken(authorization))
                checkTokenExpire()
            } catch (e: Exception) {
                throw e
            }
        } else {
            throw NotAuthorizedException("โปรด Login เพื่อขอ Token")
        }
    }

    private fun checkTokenExpire() {
        if (token.isExpire) throw NotAuthorizedException("Token expire ${token.expireDate}")
    }

    private fun findToken(tokenStr: String) =
        tokens.find(token = tokenStr) ?: throw NotAuthorizedException("โปรด Login เพื่อขอ Token")

    private fun getBasicToken(authorization: List<String>): String {
        if (authorization[0].startsWith("Basic ")) {
            throw NotAuthorizedException("is basic auth")
        }
        return authorization[0].replaceFirst(AUTHENTICATION_SCHEME, "").trim()
    }
}
