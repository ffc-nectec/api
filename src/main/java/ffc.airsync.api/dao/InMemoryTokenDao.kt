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
import ffc.entity.StorageOrg
import ffc.entity.TokenMessage
import java.util.*
import javax.ws.rs.NotAuthorizedException

class InMemoryTokenDao : TokenDao {

    private constructor()

    companion object {
        val instant = InMemoryTokenDao()
    }

    val tokenList = arrayListOf<StorageOrg<TokenMessage>>()

    override fun removeByOrgUuid(orgUUID: UUID) {
        tokenList.removeIf { it.uuid == orgUUID }

    }

    override fun insert(token: UUID, uuid: UUID, user: String, orgId: String, type: TokenMessage.TYPEROLE): TokenMessage { //uuid is orgUuid
        //1 User per 1 Token
        tokenList.removeIf { it.uuid == uuid && it.user == user }
        val tokenObj = TokenMessage(token = token, name = user, role = type)

        printDebug("InMemoryTokenDao")

        printDebug("\tToken = $token")

        tokenList.add(StorageOrg(
                uuid = uuid,
                data = tokenObj,
                user = user,
                orgId = orgId))

        printDebug("\tToken insert. Before add token")
        tokenList.forEach {
            printDebug(it)
        }

        return tokenObj

    }

    override fun updateFirebaseToken(token: UUID, firebaseToken: String) {

        val mobile = tokenList.find {
            it.data.token == token
        }
        mobile!!.data.firebaseToken = firebaseToken
    }


    override fun find(token: UUID): StorageOrg<TokenMessage> {
        val tokenObj = tokenList.find { it.data.token == token }
        if (tokenObj == null) throw NotAuthorizedException("Not Auth")
        return tokenObj
    }

    override fun findByOrgUuid(orgUUID: UUID): List<StorageOrg<TokenMessage>> {//return org > mobile
        val mobileListInOrg = tokenList.filter {
            it.uuid == orgUUID
        }
        return mobileListInOrg
    }

    override fun remove(token: UUID) {
        tokenList.removeIf { it.data.token == token }
    }
}
