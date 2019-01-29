package ffc.airsync.api.services.user.legal

import ffc.entity.User
import java.io.File

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

        private fun readPrivacy() = LegalDocument(LegalDocument.Type.privacy, File(System.getenv("PRIVACY_PATH")).readText())

        private fun readTerms() = LegalDocument(LegalDocument.Type.terms, File(System.getenv("TERMS_PATH")).readText())

        fun refresh() {
            privacy = readPrivacy()
            terms = readTerms()
        }
    }
}

internal fun User.agreementWith(doc: LegalDocument, agreementDao: LegalAgreementDao = LEGAL_AGREEMENTS): Agreement? {
    val agreement = agreementDao.lastAgreementOf(this, doc.type)
    return if (agreement?.version == doc.version) agreement else null
}

internal fun User.agreeWith(doc: LegalDocument, dao: LegalAgreementDao = LEGAL_AGREEMENTS): Boolean {
    try {
        dao.saveAgreement(this, doc.type, Agreement(doc.version))
        return true
    } catch (exception: Exception) {
        return false
    }
}
