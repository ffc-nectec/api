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

package ffc.airsync.api.services.genogram

import ffc.airsync.api.services.Dao
import ffc.entity.Person
import ffc.genogram.GenderLabel
import kotlin.math.absoluteValue

interface GenoGramDao : Dao {
    fun get(orgId: String, personId: String): List<Person.Relationship>
    fun update(orgId: String, personId: String, relation: List<Person.Relationship>): List<Person.Relationship>
    fun collectGenogram(orgId: String, personId: String): List<Person>

    fun removeByOrgId(orgId: String)

    fun addRelation(
        orgId: String,
        block: Int,
        relation: Map<String, List<Person.Relationship>>
    ): Map<String, List<Person.Relationship>>

    @Deprecated("ไม่จำเป็นต้องใช้")
    fun unConfirmBlock(orgId: String, block: Int)

    @Deprecated("ไม่จำเป็นต้องใช้")
    fun getBlock(orgId: String, block: Int): Map<String, List<Person.Relationship>>
}

fun Person.buildGeogramPerson(group: List<Person>): ffc.genogram.Person {
    val person = ffc.genogram.Person(
        idCard = id.hashCode().absoluteValue,
        firstname = firstname,
        lastname = lastname,
        gender = if (sex == Person.Sex.FEMALE) GenderLabel.FEMALE else GenderLabel.MALE,
        father = fatherId?.hashCode()?.absoluteValue,
        mother = motherId?.hashCode()?.absoluteValue,
        exHusband = null,
        twin = null,
        exWife = null,
        husband = group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.MALE)
                listOf(it.hashCode().absoluteValue)
            else
                null
        },
        wife = group.find { it.id == spouseId }.let {
            if (it?.sex == Person.Sex.FEMALE)
                listOf(it.hashCode().absoluteValue)
            else
                null
        },
        children = childId.map { it.hashCode().absoluteValue }
    )
    if ((person.children?.isEmpty() != false)) {
        person.children = null
    }

    person.properties = this

    return person
}

val personRelationsShip: GenoGramDao by lazy { MongoRelationsShipDao() }
