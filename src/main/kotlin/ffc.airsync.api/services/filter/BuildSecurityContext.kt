package ffc.airsync.api.services.filter

import ffc.entity.Token
import ffc.entity.User
import java.security.Principal
import javax.ws.rs.core.SecurityContext

class BuildSecurityContext(val token: Token, val orgId: String?, val scheme: String, val role: User.Role) :
    SecurityContext {
    private lateinit var userPrincipal: Principal

    init {
        userPrincipal = Principal { token.user.name }
    }

    override fun isUserInRole(role: String?): Boolean {
        return this.role.toString() == role
    }

    override fun getAuthenticationScheme(): String {
        return "Bearer"
    }

    override fun getUserPrincipal(): Principal {
        return userPrincipal
    }

    override fun isSecure(): Boolean {
        return true
    }
}
