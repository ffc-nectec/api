package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.User
import java.util.ArrayList
import java.util.UUID
import javax.ws.rs.NotAuthorizedException

object UserService {


    fun create(orgId: String, userList: ArrayList<User>) {
        val org = orgDao.findById(orgId)
        userList.forEach {
            printDebug("insert username " + org.name + " User = " + it.username)
            orgUser.insert(it, org)
        }
    }


    fun login(orgId: String, user: String, pass: String): TokenMessage {

        val checkUser = orgUser.isAllowById(User(user, pass), orgId)
        if (checkUser) {
            val org = orgDao.findById(orgId)
            val token = UUID.randomUUID()

            val tokenObj = ffc.airsync.api.services.module.token.create(token = token,
                    uuid = org.uuid,
                    user = user,
                    orgId = orgId,
                    type = TokenMessage.TYPEROLE.USER)
            return tokenObj
        }
        throw NotAuthorizedException("Not Auth")
    }
}
