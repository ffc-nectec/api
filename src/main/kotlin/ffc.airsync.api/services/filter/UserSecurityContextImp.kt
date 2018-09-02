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

package ffc.airsync.api.services.filter

import ffc.entity.Token
import ffc.entity.User
import java.security.Principal

class UserSecurityContextImp(override val token: Token, override val orgId: String? = null, scheme: String) : FfcSecurityContext {

    private var userPrincipal: Principal? = null
    private var scheme: String? = null

    init {

        this.scheme = scheme
        this.userPrincipal = Principal { token.user.name }
    }

    override fun isUserInRole(role: String?): Boolean {
        return User.Role.USER.toString() == role
    }

    override fun getAuthenticationScheme(): String {
        return "Bearer"
    }

    override fun getUserPrincipal(): Principal {
        return userPrincipal!!
    }

    override fun isSecure(): Boolean {
        return true
    }
}
