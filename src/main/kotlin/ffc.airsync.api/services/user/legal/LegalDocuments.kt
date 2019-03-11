package ffc.airsync.api.services.user.legal

import ffc.airsync.api.services.util.UriReader
import java.net.URI

class LegalDocuments(
    val type: LegalDocument.Type,
    val latest: LegalDocument = when (type) {
        LegalDocument.Type.privacy -> LegalDocuments.privacy
        LegalDocument.Type.terms -> LegalDocuments.terms
    }
) {
    companion object {

        private const val DEFAULT_PRIVACY_URI =
            "https://raw.githubusercontent.com/ffc-nectec/assets/master/legal/PRIVACY.md"
        private const val DEFAULT_TERMS_URI =
            "https://raw.githubusercontent.com/ffc-nectec/assets/master/legal/TERMS.md"

        var privacy: LegalDocument = readPrivacy()
            internal set
        var terms: LegalDocument = readTerms()
            internal set

        private fun readPrivacy() = LegalDocument(
            LegalDocument.Type.privacy,
            UriReader(URI(System.getenv("PRIVACY_URI") ?: DEFAULT_PRIVACY_URI)).readAsString().trim()
        )

        private fun readTerms() = LegalDocument(
            LegalDocument.Type.terms,
            UriReader(URI(System.getenv("TERMS_URI") ?: DEFAULT_TERMS_URI)).readAsString().trim()
        )

        fun refresh() {
            privacy = readPrivacy()
            terms = readTerms()
        }
    }
}
