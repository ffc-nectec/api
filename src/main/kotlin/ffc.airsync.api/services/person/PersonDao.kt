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
import ffc.airsync.api.services.Sync
import ffc.airsync.api.services.disease.findIcd10
import ffc.entity.Person
import ffc.entity.healthcare.analyze.HealthIssue

interface PersonDao : Dao, Sync<Person> {
    fun insert(orgId: String, person: Person): Person
    fun insert(orgId: String, person: List<Person>): List<Person>

    fun update(orgId: String, person: Person): Person

    fun getPerson(orgId: String, personId: String): Person
    fun getAnalyticByHouseId(orgId: String, houseId: String): Map<HealthIssue.Issue, List<HealthIssue>>

    fun findByOrgId(orgId: String): List<Person>
    fun getPeopleInHouse(orgId: String, houseId: String): List<Person>
    fun remove(orgId: String)
    fun find(query: String, orgId: String): List<Person>

    fun findHouseId(orgId: String, personId: String): String

    fun findByICD10(orgId: String, icd10: String): List<Person>
}

val persons: PersonDao by lazy { MongoPersonDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }

fun mapDeadIcd10(person: Person) {
    person.death?.causes?.findIcd10()?.let {
        person.death = Person.Death(person.death!!.date, it)
    }
}
