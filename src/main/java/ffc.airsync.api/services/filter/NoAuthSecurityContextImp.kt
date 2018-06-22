package ffc.airsync.api.services.filter

import ffc.entity.TokenMessage
import java.security.Principal

class NoAuthSecurityContextImp : FfcSecurityContext {

    private var userPrincipal: Principal? = null

    init {

        this.userPrincipal = object : Principal {
            override fun getName(): String {
                return "NOAUTH"
            }

        }

    }


    override fun isUserInRole(role: String?): Boolean {
        return TokenMessage.TYPEROLE.NOAUTH.toString().equals(role)
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

    override val token: TokenMessage?
        get() = null


    override val orgId: String?
        get() = null
}
