package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.Token
import ffc.entity.User
import java.util.ArrayList
import javax.ws.rs.NotAuthorizedException

object UserService {

    const val ORGUSER = "ORG"

    fun create(orgId: String, userList: ArrayList<User>) {
        userList.forEach {
            printDebug("insert username " + orgId + " User = " + it.name)
            orgUser.insertUser(it, orgId)

        }
    }


    fun login(orgId: String, username: String, pass: String): Token {

        val user = orgUser.getUser(username, pass, orgId)
        if (user != null) {
            return tokenDao.create(user, orgId)
        }
        throw NotAuthorizedException("Not Auth")
    }
}
