package ffc.airsync.api.services.filter

import ffc.entity.TokenMessage
import javax.ws.rs.core.SecurityContext

interface FfcSecurityContext : SecurityContext {
    val token: TokenMessage?
    val orgId: String?
}
