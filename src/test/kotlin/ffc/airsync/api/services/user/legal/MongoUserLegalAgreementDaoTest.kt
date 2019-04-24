/*
 * Copyright (c) 2562 NECTEC
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

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.resourceFile
import ffc.airsync.api.services.org.MongoOrgDao
import ffc.airsync.api.services.user.activate.Org
import ffc.entity.User
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoUserLegalAgreementDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private lateinit var dao: MongoUserLegalAgreementDao
    lateinit var maxKung: User
    lateinit var somying: User

    val terms = LegalDocument(LegalDocument.Type.terms, resourceFile("legal/TERMS.md"))
    val privacy = LegalDocument(LegalDocument.Type.privacy, resourceFile("legal/PRIVACY.md"))

    @Before
    fun setUp() {
        val orgDao = MongoOrgDao()
        orgDao.insert(Org("Dummy"))
        val org = orgDao.insert(Org("รพสตNectec"))
        maxKung = org.users.first { it.name == "maxkung" }
        somying = org.users.first { it.name == "somYing" }

        dao = MongoUserLegalAgreementDao()
    }

    @Test
    fun saveAndLoadAgreement() {
        val termAgreement = Agreement(terms.version)
        dao.saveAgreement(maxKung, terms.type, termAgreement)
        dao.saveAgreement(somying, terms.type, termAgreement)
        val privacyAgreement = Agreement(privacy.version)
        dao.saveAgreement(maxKung, privacy.type, privacyAgreement)

        dao.lastAgreementOf(maxKung, LegalDocument.Type.terms) `should equal` termAgreement
        dao.lastAgreementOf(somying, LegalDocument.Type.terms) `should equal` termAgreement
        dao.lastAgreementOf(maxKung, LegalDocument.Type.privacy) `should equal` privacyAgreement
        dao.lastAgreementOf(somying, LegalDocument.Type.privacy) `should be` null
    }

    @Test
    fun manyVersion() {
        val now = DateTime.now()
        dao.saveAgreement(maxKung, terms.type, Agreement("1", now.minusWeeks(2)))
        dao.saveAgreement(maxKung, terms.type, Agreement("2", now.minusMinutes(1)))
        dao.saveAgreement(maxKung, terms.type, Agreement("3", now))

        dao.lastAgreementOf(maxKung, LegalDocument.Type.terms)!!.let { it.version `should equal` "3" }
        dao.lastAgreementOf(maxKung, LegalDocument.Type.privacy) `should be` null
    }

    @Test
    fun noAgreeYet() {
        dao.lastAgreementOf(maxKung, LegalDocument.Type.terms) `should be` null
        dao.lastAgreementOf(somying, LegalDocument.Type.terms) `should be` null
    }

    @Test(expected = IllegalStateException::class)
    fun notFoundUser() {
        dao.lastAgreementOf(User(), LegalDocument.Type.terms)
    }
}
