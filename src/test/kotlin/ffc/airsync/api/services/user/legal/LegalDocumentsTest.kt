package ffc.airsync.api.services.user.legal

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import ffc.airsync.api.resourceFile
import ffc.entity.User
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.junit.Test

class LegalDocumentsTest {

    val mockDao = mock(LegalAgreementDao::class)

    val user = User()
    val terms = LegalDocument(LegalDocument.Type.terms, resourceFile("legal/TERMS.md"))
    val privacy = LegalDocument(LegalDocument.Type.privacy, resourceFile("legal/PRIVACY.md"))

    @Test
    fun getUserAgreementWithLegalDoc() {
        val privacyAgreement = Agreement(privacy.version)
        whenever(mockDao.lastAgreementOf(user, LegalDocument.Type.privacy)).doAnswer { privacyAgreement }
        whenever(mockDao.lastAgreementOf(user, LegalDocument.Type.terms)).doAnswer { null }

        user.agreementWith(privacy, mockDao) `should equal` privacyAgreement
        user.agreementWith(terms, mockDao) `should be` null
        user.agreementWith(LegalDocument(LegalDocument.Type.privacy, "Dummy Privacy Policy"), mockDao) `should be` null
    }

    @Test
    fun saveUserAgreement() {
        user.agreeWith(privacy, mockDao)
        user.agreeWith(terms, mockDao)

        verify(mockDao).saveAgreement(
            eq(user),
            eq(privacy.type),
            argThat { version == privacy.version }
        )

        verify(mockDao).saveAgreement(
            eq(user),
            eq(terms.type),
            argThat { version == terms.version }
        )
    }
}
