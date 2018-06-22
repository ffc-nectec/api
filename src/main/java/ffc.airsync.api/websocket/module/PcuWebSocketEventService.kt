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

package ffc.airsync.api.websocket.module

import ffc.airsync.api.dao.DaoFactory
import ffc.airsync.api.dao.OrgDao
import ffc.airsync.api.printDebug
import ffc.airsync.api.websocket.module.PcuEventService.Companion.connectionMap
import ffc.entity.Organization
import ffc.entity.TokenMessage
import ffc.entity.fromJson
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jetty.websocket.api.Session
import java.util.*

class PcuWebSocketEventService(val sess: Session) : PcuEventService {


    private var session: String = ""
    private var count = 0
    private var stage = 0  //stage 0:init   1:run
    private var organization: Organization = Organization(UUID.randomUUID()) {
        id = "-1"
    }
    var orgDao: OrgDao = DaoFactory().buildPcuDao()

    init {
        this.session = DigestUtils.sha1Hex(sess.toString())
        printDebug("onWebSocketConnect " + this.session)
        connectionMap.put(this.session, sess)
        printDebug("Test find session before add " + connectionMap.get(this.session)!!.remote.inetSocketAddress.hostName)
        printDebug("Test find session before add 2 " + connectionMap.get(this.session)!!.remote.inetSocketAddress.hostName)

    }


    override fun getSession(): String {
        return session
    }

    override fun getSessionObject(): Session {
        return sess
    }


    override fun receiveTextData(message: String) {
        printDebug("onWebSocketText " + session)
        printDebug("Stage = " + stage + " Count:" + (count++) + "\tMessage: " + message)

        if (stage == 0) {//Register Channel
            val token: TokenMessage = message.fromJson()
            val pcu = orgDao.findByToken(token.token)
            if (pcu != null) {
                //sess.remote.sendString(TokenMessage(pcu.centralToken.toString()).toJson())
                //connectionMap.put(session,sess)
                pcu.session = session
                stage = 1
            } else {
                throw SecurityException("Cannot handcheck")
            }


        } else if (stage == 1) {//Sync
            if (message.equals("H")) sess.remote.sendString("H")
            else {// Throw
                val messageerr: String = ("Connection not H IP=" + organization.lastKnownIp
                        + " Organization pcuCode = " + organization.pcuCode
                        + " Organization name = " + organization.name
                        + " Organization uuid = " + organization.uuid)
                throw SecurityException(messageerr)
            }
        }
    }
}
