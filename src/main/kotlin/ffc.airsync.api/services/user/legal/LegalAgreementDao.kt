package ffc.airsync.api.services.user.legal

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.MongoDao
import ffc.entity.User

interface LegalAgreementDao {

    fun saveAgreement(user: User, type: LegalDocument.Type, agreement: Agreement)

    fun lastAgreementOf(user: User, type: LegalDocument.Type): Agreement?
}

val LEGAL_AGREEMENTS: LegalAgreementDao by lazy { MongoUserLegalAgreementDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }

internal class MongoUserLegalAgreementDao(host: String, port: Int) : LegalAgreementDao,
    MongoDao(host, port, "ffc", "organ") {

    override fun saveAgreement(user: User, type: LegalDocument.Type, agreement: Agreement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lastAgreementOf(user: User, type: LegalDocument.Type): Agreement? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
