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
        var privacy: LegalDocument = readPrivacy()
            internal set
        var terms: LegalDocument = readTerms()
            internal set

        private fun readPrivacy() = LegalDocument(
            LegalDocument.Type.privacy,
            UriReader(URI(System.getenv("PRIVACY_URI"))).readAsString()
        )

        private fun readTerms() = LegalDocument(
            LegalDocument.Type.terms,
            UriReader(URI(System.getenv("TERMS_URI"))).readAsString()
        )

        fun refresh() {
            privacy = readPrivacy()
            terms = readTerms()
        }
    }
}
