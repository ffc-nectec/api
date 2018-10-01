package ffc.airsync.api.services.user

import ffc.airsync.api.services.token.tokens
import ffc.entity.Token
import ffc.entity.User
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotAuthorizedException

object UserService {
    fun create(orgId: String, user: List<User>): List<User> {
        return user.map { users.insertUser(it, orgId) }
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
