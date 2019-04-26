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

package ffc.airsync.api.security.otp

import ffc.airsync.api.MongoDbTestRule
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should be`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoSecretStoreTest {
    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID1 = "5bbd7f5ebc920637b04c7796"
    private val ORG_ID2 = "5bbd7f5ebc920637b04c7799"
    lateinit var secretStore: SecretStore

    @Before
    fun setUp() {
        secretStore = MongoSecretStore()
    }

    @Test
    fun secretOf() {
        val secretKeyOrg1 = secretStore.secretOf(ORG_ID1)
        val secretKeyOrg2 = secretStore.secretOf(ORG_ID2)

        secretStore.secretOf(ORG_ID1) `should be equal to` secretKeyOrg1
        secretStore.secretOf(ORG_ID2) `should be equal to` secretKeyOrg2
    }

    @Test
    fun mongoSecretStoreGetEmpty() {
        val secretStore = secretStore as MongoSecretStore
        secretStore.getSecret(ORG_ID1) `should be` null
    }

    @Test
    fun mongoSecretStoreCreateAndGet() {
        val secretStore = secretStore as MongoSecretStore
        secretStore.createSecret(ORG_ID1)
        secretStore.getSecret(ORG_ID1)!!.length `should be greater than` 20
    }
}
