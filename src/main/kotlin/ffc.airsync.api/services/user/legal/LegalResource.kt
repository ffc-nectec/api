package ffc.airsync.api.services.user.legal

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.user.users
import org.joda.time.DateTime
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
class LegalResource {

    private val privacy by lazy { LegalDocuments(LegalDocument.Type.privacy) }

    private val terms by lazy { LegalDocuments(LegalDocument.Type.terms) }

    @GET
    @Path("/legal/privacy/latest")
    @Cache(maxAge = 3600 * 24)
    @Produces("text/markdown; charset=utf-8")
    fun getPrivacy(): String {
        return privacy.latest.content
    }

    @GET
    @Path("/legal/terms/latest")
    @Cache(maxAge = 3600 * 24)
    @Produces("text/markdown; charset=utf-8")
    fun getTerms(): String {
        return terms.latest.content
    }

    @GET
    @Path("/legal/refresh")
    @Cache(maxAge = 3600)
    fun refresh(): Map<String, Any> {
        LegalDocuments.refresh()
        return mapOf(
            "message" to "refreshed",
            "timestamp" to DateTime.now()
        )
    }

    @GET
    @Path("org/$ORGIDTYPE/user/{userId}/agreement/privacy/latest")
    @Cache(maxAge = 3600)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun checkPrivacyAgreementOf(
        @PathParam("orgUuid") orgId: String,
        @PathParam("userId") userId: String
    ): Agreement {
        val user = users.getUserById(orgId, userId)
        return user.agreementWith(privacy.latest) ?: throw NotFoundException()
    }

    @GET
    @Path("org/$ORGIDTYPE/user/{userId}/agreement/terms/latest")
    @Cache(maxAge = 3600)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun checkTermsAgreementOf(
        @PathParam("orgUuid") orgId: String,
        @PathParam("userId") userId: String
    ): Agreement {
        val user = users.getUserById(orgId, userId)
        return user.agreementWith(privacy.latest) ?: throw NotFoundException()
    }

    @PUT
    @Path("org/$ORGIDTYPE/user/{userId}/agreement/privacy/{version}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun agreePrivacy(
        @PathParam("orgUuid") orgId: String,
        @PathParam("userId") userId: String,
        @PathParam("version") version: String
    ) {
        require(version == privacy.latest.version) { "Not acceptable Privacy Policy's version [$version]" }
        users.getUserById(orgId, userId).agreeWith(privacy.latest)
    }

    @PUT
    @Path("org/$ORGIDTYPE/user/{userId}/agreement/terms/{version}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun agreeTerms(
        @PathParam("orgUuid") orgId: String,
        @PathParam("userId") userId: String,
        @PathParam("version") version: String
    ) {
        require(version == terms.latest.version) { "Not acceptable Terms of Uses's version [$version]" }
        users.getUserById(orgId, userId).agreeWith(terms.latest)
    }
}
