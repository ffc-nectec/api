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
    private val pattern = Pattern.compile("""^org/(?<orgId>[\w\d]+)/?.*$""")
    private val logger by lazy { getLogger() }
    override fun filter(requestContext: ContainerRequestContext) {
        val urlScheme = requestContext.uriInfo.baseUri.scheme
        val baseUrl = requestContext.uriInfo.path.toString()
        val matcherOrgId = pattern.matcher(baseUrl)
        var orgId = ""

        if (matcherOrgId.find()) {
            orgId = matcherOrgId.group(1)
        }
        val authenInfo: BasicTokenInfo

        logger.debug(
            "Create BasicAuthFilter " +
                "orgId=$orgId " +
                "matchOrgIdCount=${matcherOrgId.groupCount()} " +
                "baseUrl=$baseUrl"
        )
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
