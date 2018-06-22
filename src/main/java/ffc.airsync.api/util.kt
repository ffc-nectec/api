/*
 * Copyright (c) 2018 NECTEC
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

package ffc.airsync.api

import org.joda.time.DateTime
import java.util.*

val USERDATEEXPIRE = 1
val ORGDATEEXPIRE = 9000

data class TokenMessage(val token: UUID, var firebaseToken: String? = null, val timestamp: DateTime = DateTime.now(), val role: TYPEROLE = TYPEROLE.NOAUTH, val name: String) {

    // fun getExpireDate(temp :String? = null): DateTime = timestamp.plusDays(USERDATEEXPIRE)
    var expireDate: DateTime

    init {
        when (role) {
            TYPEROLE.USER -> expireDate = timestamp.plusDays(USERDATEEXPIRE)
            TYPEROLE.ORG -> expireDate = timestamp.plusDays(ORGDATEEXPIRE)
            else -> expireDate = timestamp
        }
    }

    fun checkExpireTokem(): Boolean = expireDate.isBeforeNow


    enum class TYPEROLE {
        ORG, USER, NOAUTH
    }
}

val debug = System.getenv("FFC_DEBUG")
fun <T> printDebug(infoDebug: T) {
    if (debug == null)
        println(infoDebug)
}
