package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.Token
import ffc.entity.User
import java.util.ArrayList
import javax.ws.rs.NotAuthorizedException

object UserService {


    fun create(orgId: String, userList: ArrayList<User>) {
        userList.forEach {
            printDebug("insert username " + orgId + " User = " + it.name)
            orgUser.insertUser(it, orgId)

        }
    }


    fun login(orgId: String, user: String, pass: String): Token {

        if (orgUser.isAllowUser(user, pass, orgId)) {
            return tokenDao.create(user, orgId, Token.TYPEROLE.USER)
        }
        throw NotAuthorizedException("Not Auth")
    }
}
