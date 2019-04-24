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

package ffc.airsync.api.services.user

import ffc.airsync.api.security.token.tokens
import ffc.entity.Token
import ffc.entity.User
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotAuthorizedException

object UserService {
    fun create(orgId: String, user: List<User>): List<User> {
        return user.map { users.insertUser(it, orgId) }
    }

    fun login(orgId: String, username: String, pass: String): Token {
        if (UserDao.isBlockUser(username)) throw ForbiddenException("User ไม่มีสิทธิ์ในการใช้งาน")
        val user = users.findThat(orgId, username, pass)
        if (user != null) {
            user.orgId = orgId
            return tokens.create(user, orgId)
        }
        throw NotAuthorizedException("Not Auth")
    }
}
