package ffc.airsync.api.services.user.legal

import ffc.airsync.api.services.util.md5

data class LegalDocument(
    val type: Type,
    val content: String
) {
    val version: String = content.md5()

    enum class Type {
        terms, privacy
    }
}
