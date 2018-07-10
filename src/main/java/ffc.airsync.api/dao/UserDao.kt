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

package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.User

interface UserDao : Dao {

    companion object {
        private val userBlock = arrayListOf<String>("ADM", "adm", "newuser", "usr_db", "Drug_Store_Admin")
        fun isBlockUser(name: String): Boolean {
            printDebug("Check block user.")
            val findUser = userBlock.find {
                it == name.trim()
            }
            printDebug("\tResult block check $findUser")
            return (findUser != null)
        }
    }

    fun insertUser(user: User, orgId: String): User
    fun updateUser(user: User, orgId: String): User
    fun findUser(orgId: String): List<User>
    fun findThat(orgId: String, name: String, pass: String): User?
}
