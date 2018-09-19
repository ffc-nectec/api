/*
 * Copyright (c) 2561 NECTEC
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

package ffc.airsync.api.services

import ffc.entity.User
import java.util.Enumeration
import java.util.HashMap
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.core.SecurityContext
fun HttpServletRequest.buildHeaderMap(): Map<String, String> {
    val map = HashMap<String, String>()
    val headerNames: Enumeration<String> = this.headerNames
    while (headerNames.hasMoreElements()) {
        val key = headerNames.nextElement() as String
        val value = this.getHeader(key)
        map[key] = value
    }
    return map
}

const val GEOJSONHeader = "application/vnd.geo+json"

fun getTokenRole(context: SecurityContext): User.Role {
    val roleList = User.Role.values()

    val role = roleList.find {
        context.isUserInRole(it.toString())
    } ?: throw NotAuthorizedException("Token นี้ไม่มีสิท")

    return role
}
