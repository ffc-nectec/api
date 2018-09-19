package ffc.airsync.api.services.filter

import ffc.entity.Token
import javax.ws.rs.core.SecurityContext

interface FfcSecurityContext : SecurityContext {
    val token: Token?
    val orgId: String?
}
