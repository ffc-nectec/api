package ffc.airsync.api.services.user.legal

import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.toDocument
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document

interface LegalAgreementDao {

    fun saveAgreement(user: User, type: LegalDocument.Type, agreement: Agreement)

    fun lastAgreementOf(user: User, type: LegalDocument.Type): Agreement?
}

val LEGAL_AGREEMENTS: LegalAgreementDao by lazy { MongoUserLegalAgreementDao() }

internal class MongoUserLegalAgreementDao : LegalAgreementDao, MongoDao("ffc", "organ") {

    override fun saveAgreement(user: User, type: LegalDocument.Type, agreement: Agreement) {
        println("user.name = ${user.name}")
        val agreement = "users.$.${type.name}" equal agreement.toDocument()
        val agreementPush = "\$push" equal agreement
        dbCollection.updateOne("users.id" equal user.id, agreementPush)
    }

    override fun lastAgreementOf(user: User, type: LegalDocument.Type): Agreement? {
        val orgDoc = dbCollection
            .find("users.id" equal user.id)
            .projection("users" equal 1)
            .first()
        check(orgDoc != null) { "Not found user match id ${user.id}" }

        val usersDoc = orgDoc.get("users") as List<Document>
        val agreementDoc = usersDoc.first { it.get("id") == user.id }.get(type.name) as List<Document>?
        return agreementDoc?.toJson()?.parseTo<List<Agreement>>()?.sortedByDescending { it.agreeTime }?.first()
    }
}
