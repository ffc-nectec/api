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

package ffc.airsync.api.security.token

import ffc.airsync.api.getLogger
import ffc.airsync.api.security.ApiSecurityContext
import ffc.airsync.api.security.UserPrincipal
import java.util.regex.Pattern
import javax.annotation.Priority
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

@Priority(Priorities.AUTHENTICATION)
@Provider
class TokenAuthFilter : ContainerRequestFilter {

    private val logger by lazy { getLogger() }
    override fun filter(requestContext: ContainerRequestContext) {
        val requestToken = requestContext.token ?: return
        val requestOrg = requestContext.orgId ?: return

        val token = tokens.token(requestToken, orgId = requestOrg)
            ?: throw NotAuthorizedException("ข้อมูลการยืนยันตัวตนไม่ถูกต้อง")

        if (token.isExpire) {
            throw NotAuthorizedException("กรุณาทำการยืนยันตัวตนใหม่")
        }

        requestContext.securityContext = ApiSecurityContext(token, requestContext.uriInfo.baseUri.scheme)
        requestContext.securityContext.let {
            val user = (it.userPrincipal as UserPrincipal).user
            logger.info("${user.orgId}.${user.id} use token ${requestToken.substring(0, 7)} at " +
                "${requestContext.method} ${requestContext.uriInfo.path}  ")
        }
    }

    val ContainerRequestContext.orgId: String?
        get() {
            val matcher = orgPattern.matcher(uriInfo.path.toString())
            return if (matcher.find()) {
                matcher.group(1)
            } else null
        }

    val ContainerRequestContext.token: String?
        get() {
            val authHeaders = headers[AUTHORIZATION_HEADER]
            if (authHeaders.isNullOrEmpty())
                return null
            val bearer = authHeaders.find { it.startsWith(BEARER_SCHEME) }
            return bearer?.replaceFirst(BEARER_SCHEME, "")?.trim()
        }

    companion object {
        val AUTHORIZATION_HEADER = "Authorization"
        val BEARER_SCHEME = "Bearer "
        private val orgPattern = Pattern.compile("""^org/(?<orgId>[\w\d]+)/?.*$""")
    }
}
