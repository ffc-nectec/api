package ffc.airsync.api.services.module

import ffc.airsync.api.dao.UserDao
import ffc.airsync.api.printDebug
import ffc.entity.Token
import ffc.entity.User
import java.util.ArrayList
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotAuthorizedException

object UserService {
    const val ORGUSER = "ORG"

    fun create(orgId: String, users: ArrayList<User>): List<User> {
        val usersUpdate = arrayListOf<User>()
        users.forEach {
            printDebug("insert username " + orgId + " User = " + it.name)
            usersUpdate.add(ffc.airsync.api.services.module.users.insertUser(it, orgId))
        }
        return usersUpdate
    }

    fun login(orgId: String, username: String, pass: String): Token {
        if (UserDao.isBlockUser(username)) throw ForbiddenException("User ไม่มีสิทธิ์ในการใช้งาน")
        val user = users.findThat(orgId, username, pass)
        if (user != null) {
            return tokens.create(user, orgId)
        }
        throw NotAuthorizedException("Not Auth")
    }
}
