package ffc.airsync.api.services.filter

import ffc.entity.Token
import java.security.Principal

class NoAuthSecurityContextImp : FfcSecurityContext {

    private var userPrincipal: Principal? = null

    init {
        this.userPrincipal = Principal { "NOAUTH" }
    }
    override fun isUserInRole(role: String?): Boolean {
        return "" == role
    }

    override fun getAuthenticationScheme(): String {
        return ""
    }

    override fun getUserPrincipal(): Principal {
        return userPrincipal!!
    }

    override fun isSecure(): Boolean {
        return true
    }

    override val token: Token?
        get() = null
    override val orgId: String?
        get() = null
}
