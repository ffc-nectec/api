package ffc.airsync.api.services.filter

import ffc.airsync.api.TokenMessage
import ffc.entity.Token
import java.security.Principal

class OrgSecurityContextImp(override val token: Token, override val orgId: String? = null, scheme: String) : FfcSecurityContext {

    private var HTTPS = "https://"
    private var userPrincipal: Principal? = null
    private var scheme: String? = null


    init {
        this.scheme = scheme

        this.userPrincipal = Principal { token.name }

    }


    override fun isUserInRole(role: String?): Boolean {
        return TokenMessage.TYPEROLE.ORG.toString() == role
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
