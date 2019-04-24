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
 */

package ffc.airsync.api.security

import ffc.airsync.api.getLogger
import ffc.airsync.api.services.token.tokens
import ffc.entity.Token
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.container.ContainerRequestContext

class BasicTokenInfo(requestContext: ContainerRequestContext, orgId: String) {
    val AUTHORIZATION_PROPERTY = "Authorization"
    val AUTHENTICATION_SCHEME = "Bearer "
    val token: Token
    private val logger by lazy { getLogger() }

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
