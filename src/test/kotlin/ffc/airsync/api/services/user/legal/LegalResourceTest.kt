package ffc.airsync.api.services.user.legal

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import ffc.airsync.api.filter.RequireError
import ffc.airsync.api.services.user.UserDao
import ffc.entity.User
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.Test
import javax.ws.rs.core.Application

class LegalResourceTest : JerseyTest() {

    private lateinit var dummyPrivacy: LegalDocuments
    private lateinit var dummyTerms: LegalDocuments

    private lateinit var dummyUser: User

    private lateinit var mockUsers: UserDao
    private lateinit var mockAgreementDao: LegalAgreementDao

    override fun configure(): Application {
        dummyPrivacy = LegalDocuments(
            LegalDocument.Type.privacy,
            LegalDocument(LegalDocument.Type.privacy, "# Privacy Policy - version 1"))
        dummyTerms = LegalDocuments(
            LegalDocument.Type.terms,
            LegalDocument(LegalDocument.Type.terms, "# Terms of user - version 1"))
        dummyUser = User("5c21d3c66d5a5600047f7345").apply {
            orgId = "5c21d3b76d5a5600047f7334"
        }

        mockUsers = mock {
            on { it.getUserById(dummyUser.orgId!!, dummyUser.id) }.doAnswer { dummyUser }
        }
        mockAgreementDao = mock {}

        return ResourceConfig()
            .registerClasses(RequireError::class.java)
            .register(LegalResource(
                privacy = dummyPrivacy,
                terms = dummyTerms,
                usersDao = mockUsers,
                legalDao = mockAgreementDao))
    }

    @Test
    fun getLatestPrivacy() {
        val res = target("legal/privacy/latest").request().get()

        res.status `should be equal to` 200
        res.headers.get("Content-Type").toString() `should contain` "text/markdown"
        res.readEntity(String::class.java) `should be equal to` dummyPrivacy.latest.content
    }

    @Test
    fun getLatestTermsOfUse() {
        val res = target("legal/terms/latest").request().get()

        res.status `should be equal to` 200
        res.headers.get("Content-Type").toString() `should contain` "text/markdown"
        res.readEntity(String::class.java) `should be equal to` dummyTerms.latest.content
    }

    @Test
    fun checkAgreement() {
        whenever(mockAgreementDao.lastAgreementOf(dummyUser, LegalDocument.Type.privacy))
            .thenAnswer { Agreement(dummyPrivacy.latest.version) }

        val res = target("legal/privacy/latest/agreement/${dummyUser.orgId}/${dummyUser.id}").request().get()

        println(res.readEntity(String::class.java))
        res.status `should be equal to` 200
    }

    @Test
    fun notFoundAgreement() {
        val res = target("legal/privacy/latest/agreement/${dummyUser.orgId}/${dummyUser.id}").request().get()

        res.status `should be equal to` 404
    }

    @Test
    fun agreeTerm() {
        val res = target("legal/terms/${dummyTerms.latest.version}/agreement/${dummyUser.orgId}/${dummyUser.id}")
            .request().post(null)

        res.status `should be equal to` 204
    }

    @Test
    fun agreeNotLatestTerm() {
        val res = target("legal/terms/1253ds4djh/agreement/${dummyUser.orgId}/${dummyUser.id}")
            .request().post(null)

        res.status `should be equal to` 400
    }
}
