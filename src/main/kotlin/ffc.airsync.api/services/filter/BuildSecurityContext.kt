package ffc.airsync.api.services.filter

import ffc.entity.Token
import ffc.entity.User
import java.security.Principal
import javax.ws.rs.core.SecurityContext

class BuildSecurityContext(val token: Token, val orgId: String?, val scheme: String) :
    SecurityContext {
    private var userPrincipal: Principal

    init {
        userPrincipal = Principal { token.user.name }
    }

    override fun isUserInRole(role: String?): Boolean {
        if (role == null) return false
        return token.user.roles.contains(User.Role.valueOf(role))
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
