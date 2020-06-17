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
 *
 */

package ffc.airsync.api.services.user

import ffc.airsync.api.services.Dao
import ffc.entity.User

interface UserDao : Dao {
    companion object {
        private val userBlock = listOf("ADM", "adm", "newuser", "usr_db", "Drug_Store_Admin", "Student_Update")

        fun isBlockUser(name: String) = userBlock.firstOrNull { it == name.trim() } != null
    }

    fun insert(user: User, orgId: String): User
    fun update(user: User, orgId: String, updatePassword: Boolean = false): User
    fun delete(orgId: String, userId: List<String>): HashMap<String, Boolean>

    fun getUserByName(orgId: String, name: String): User?
    fun getUserById(orgId: String, userId: String): User
    fun findUser(orgId: String): List<User>
    fun findThat(orgId: String, name: String, password: String): User?
    fun updatePassword(orgId: String, username: String, password: String): User
}

val users: UserDao by lazy { MongoUserDao() }
