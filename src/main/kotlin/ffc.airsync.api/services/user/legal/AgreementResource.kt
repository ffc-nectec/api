/*
 * Copyright (c) 2019 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ffc.airsync.api.services.user.legal

import ffc.airsync.api.filter.cache.Cache
import ffc.airsync.api.getLogger
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.user.UserDao
import ffc.airsync.api.services.user.users
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/org")
@Produces(MediaType.APPLICATION_JSON)
class AgreementResource(
    private val privacy: LegalDocuments = LegalDocuments(LegalDocument.Type.privacy),
    private val terms: LegalDocuments = LegalDocuments(LegalDocument.Type.terms),
    var legalDao: LegalAgreementDao? = LEGAL_AGREEMENTS,
    var usersDao: UserDao? = users
) {
    val logger by lazy { getLogger() }

    @GET
    @Path("/$ORGIDTYPE/user/{userId}/agreement/privacy/latest")
    @Cache(maxAge = 3600)
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun checkPrivacyAgreementOf(
        @PathParam("orgId") orgId: String,
        @PathParam("userId") userId: String
    ): Agreement {
        val user = usersDao!!.getUserById(orgId, userId)
        return user.agreementWith(privacy.latest, legalDao!!)
            ?: throw NotFoundException("ไม่พบการยอมรับนโยบายความเป็นส่วนตัวฉบับล่าสุด")
    }

    @GET
    @Path("/$ORGIDTYPE/user/{userId}/agreement/terms/latest")
    @Cache(maxAge = 3600)
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun checkTermsAgreementOf(
        @PathParam("orgId") orgId: String,
        @PathParam("userId") userId: String
    ): Agreement {
        val user = usersDao!!.getUserById(orgId, userId)
        return user.agreementWith(privacy.latest, legalDao!!)
            ?: throw NotFoundException("ไม่พบการยอมรับเงื่อนไขการใช้งานฉบับล่าสุด")
    }

    @POST
    @Path("/$ORGIDTYPE/user/{userId}/agreement/privacy/{version}")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun agreePrivacy(
        @PathParam("orgId") orgId: String,
        @PathParam("userId") userId: String,
        @PathParam("version") version: String
    ): Response {
        require(version == privacy.latest.version) { "Not acceptable Privacy Policy's version [$version]" }
        usersDao!!.getUserById(orgId, userId).agreeWith(privacy.latest, legalDao!!)
        logger.info("user[$userId] accept privacy[$version]")
        return Response.status(201).build()
    }

    @POST
    @Path("/$ORGIDTYPE/user/{userId}/agreement/terms/{version}")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR", "PATIENT")
    fun agreeTerms(
        @PathParam("orgId") orgId: String,
        @PathParam("userId") userId: String,
        @PathParam("version") version: String
    ): Response {
        require(version == terms.latest.version) { "Not acceptable Terms of Uses's version [$version]" }
        usersDao!!.getUserById(orgId, userId).agreeWith(terms.latest, legalDao!!)
        logger.info("user[$userId] accept terms[$version]")
        return Response.status(201).build()
    }
}
