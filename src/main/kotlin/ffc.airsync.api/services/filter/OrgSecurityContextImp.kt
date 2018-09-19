package ffc.airsync.api.services.filter

import ffc.entity.Token
import ffc.entity.User
import java.security.Principal

class OrgSecurityContextImp(override val token: Token, override val orgId: String? = null, scheme: String) : FfcSecurityContext {

    private var HTTPS = "https://"
    private var userPrincipal: Principal? = null
    private var scheme: String? = null
    init {
        this.scheme = scheme

        this.userPrincipal = Principal { token.user.name }
    }
    override fun isUserInRole(role: String?): Boolean {
        return User.Role.ORG.toString() == role
    }

    override fun getAuthenticationScheme(): String {
        return "Bearer"
    }

    override fun getUserPrincipal(): Principal {
        return userPrincipal!!
    }

    override fun isSecure(): Boolean {
        return true
    }
}
