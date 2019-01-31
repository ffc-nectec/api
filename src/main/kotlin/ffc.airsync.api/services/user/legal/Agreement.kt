package ffc.airsync.api.services.user.legal

import ffc.entity.User
import org.joda.time.DateTime

data class Agreement(
    val version: String,
    val agreeTime: DateTime = DateTime.now()
)

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
