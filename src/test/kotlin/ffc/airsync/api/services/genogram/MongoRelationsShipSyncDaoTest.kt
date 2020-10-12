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

import ffc.airsync.api.MongoDbTestRule
import ffc.airsync.api.services.person.MongoPersonDao
import ffc.airsync.api.services.person.PersonDao
import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.healthcare.Chronic
import ffc.entity.healthcare.Icd10
import org.amshove.kluent.`should be equal to`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoRelationsShipSyncDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    lateinit var dao: GenoGramDao
    lateinit var daoPerson: PersonDao
    lateinit var somChai: Person
    lateinit var somYing: Person
    lateinit var rabbit: Person

    val `สมชาย` = Person().apply {
        identities.add(ThaiCitizenId("1231233123421"))
        prename = "นาย"
        firstname = "สมชาย"
        lastname = "โคตรกระบือ"
        sex = Person.Sex.MALE
        birthDate = LocalDate.now().minusYears(20)
        chronics.add(Chronic(Icd10("fair", "dxabc00x")))
        chronics.add(Chronic(Icd10("fair", "abcffe982")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "12345678901"
    }
    val `สมหญิง` = Person().apply {
        identities.add(ThaiCitizenId("2123455687675"))
        prename = "นางสาว"
        firstname = "สมหญิง"
        lastname = "สมบูรณ์จิต"
        sex = Person.Sex.FEMALE
        birthDate = LocalDate.now().minusYears(27)
        chronics.add(Chronic(Icd10("floor", "I10")))
        chronics.add(Chronic(Icd10("fary", "I11")))
        link = Link(System.JHICS)
        link!!.isSynced = true
        houseId = "11111111111"
    }
    val `กระต่าย` = Person().apply {
        identities.add(ThaiCitizenId("1122399087432"))
        prename = "นางสาว"
        firstname = "กระต่าย"
        lastname = "สุดน่ารัก"
        sex = Person.Sex.FEMALE
        birthDate = LocalDate.now().minusYears(22)
        chronics.add(Chronic(Icd10("sleep", "I10")))
        chronics.add(Chronic(Icd10("god", "I11")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "99887744998"
    }

    @Before
    fun initDb() {
        daoPerson = MongoPersonDao()
        dao = MongoRelationsShipDao()

        somChai = daoPerson.insert(ORG_ID, `สมชาย`)
        somYing = daoPerson.insert(ORG_ID, `สมหญิง`)
        rabbit = daoPerson.insert(ORG_ID, `กระต่าย`)

        somChai.relationships.add(Person.Relationship(Person.Relate.Child, somYing))
        somYing.relationships.add(Person.Relationship(Person.Relate.Father, somChai))
    }

    @Test
    fun insertBlock() {
        val relation = HashMap<String, List<Person.Relationship>>()
        relation[somChai.id] = somChai.relationships
        relation[somYing.id] = somYing.relationships

        val result = dao.addRelation(ORG_ID, 1, relation)

        result.count() `should be equal to` 2
        result[somChai.id]!!.size `should be equal to` somChai.relationships.size
        result[somYing.id]!!.size `should be equal to` somYing.relationships.size
    }

    @Test
    fun getBlockManyPerson() {
        val relation = HashMap<String, List<Person.Relationship>>()
        relation[somChai.id] = somChai.relationships
        relation[somYing.id] = somYing.relationships

        dao.addRelation(ORG_ID, 1, relation)

        dao.getBlock(ORG_ID, 1).size `should be equal to` 2
        daoPerson.getPerson(ORG_ID, somChai.id).relationships.size `should be equal to` 1
    }

    @Test
    fun removeByOrgId() {
        val relation = HashMap<String, List<Person.Relationship>>()
        relation[somChai.id] = somChai.relationships
        relation[somYing.id] = somYing.relationships

        dao.addRelation(ORG_ID, 1, relation)
        dao.removeByOrgId(ORG_ID)

        dao.getBlock(ORG_ID, 1).size `should be equal to` 0
    }
}
