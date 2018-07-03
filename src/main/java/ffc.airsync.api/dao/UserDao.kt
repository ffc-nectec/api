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
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.ws.rs.ForbiddenException

interface UserDao {

    companion object {
        private val userBlock = arrayListOf<String>("ADM", "adm", "newuser", "usr_db", "Drug_Store_Admin")
        fun checkBlockUser(name: String) {
            printDebug("Check block user.")
            val findUser = userBlock.find {
                it == name.trim()
            }
            printDebug("\tResult block check $findUser")
            if (findUser != null) throw ForbiddenException("User ไม่มีสิทธ์")
        }
    }

    fun insertUser(user: User, orgId: String)
    fun updateUser(user: User, orgId: String)
    fun findUser(orgId: String): List<User>
    fun getUser(name: String, pass: String, orgId: String): User?

    fun getPass(password: String, SALT_PASS: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encoded = digest.digest(("$password$SALT_PASS$password").toByteArray(StandardCharsets.UTF_8))

        val hexString = StringBuffer()
        for (i in 0 until encoded.size) {
            val hex = Integer.toHexString(0xff and encoded[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
