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

package ffc.airsync.api.services.token

import ffc.airsync.api.services.Dao
import ffc.entity.Token
import ffc.entity.User

interface TokenDao : Dao {
    fun create(user: User, orgId: String): Token
    fun login(token: String, orgId: String): Token?
    fun findByOrgId(orgId: String): List<Token>
    fun remove(token: String): Boolean
    fun removeByOrgId(orgId: String)
}

val tokens: TokenDao by lazy { MongoTokenDao() }
