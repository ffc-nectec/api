/*
 * Copyright (c) 2561 NECTEC
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
 */

package ffc.airsync.api.services.person

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.entity.Entity
import ffc.entity.Person

interface PersonDao : Dao {
    fun insert(orgId: String, person: Person): Person
    fun insert(orgId: String, personList: List<Person>): List<Person>
    fun getPerson(orgId: String, personId: String): Person

    fun findByOrgId(orgId: String): List<Person>
    fun getPeopleInHouse(orgId: String, houseId: String): List<Person>
    fun remove(orgId: String)
    fun find(query: String, orgId: String): List<Person>

    fun findByICD10(orgId: String, icd10: String): List<Person>
    override fun syncData(orgId: String, limitOutput: Int): List<Entity>
}

val persons: PersonDao by lazy { MongoPersonDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
