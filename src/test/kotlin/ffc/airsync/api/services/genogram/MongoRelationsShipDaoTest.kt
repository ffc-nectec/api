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
import ffc.entity.update
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoRelationsShipDaoTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    private val ORG_ID = "5bbd7f5ebc920637b04c7796"
    private val ORG_ID2 = "5bbd7f5ebc920637b04c7797"
    lateinit var dao: GenoGramDao
    lateinit var daoPerson: PersonDao
    lateinit var somChai: Person
    lateinit var somYing: Person
    lateinit var rabbit: Person
    lateinit var katar: Person
    lateinit var karog: Person

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
    val `กระแต` = Person().apply {
        identities.add(ThaiCitizenId("2343211234565"))
        prename = "นางสาว"
        firstname = "กระแต"
        lastname = "ปรีดแตก"
        sex = Person.Sex.FEMALE
        birthDate = LocalDate.now().minusYears(18)
        chronics.add(Chronic(Icd10("sleep", "I12")))
        chronics.add(Chronic(Icd10("god", "I15")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "99887744903"
    }
    val `กระรอก` = Person().apply {
        identities.add(ThaiCitizenId("2343210934516"))
        prename = "นาย"
        firstname = "กระรอก"
        lastname = "ศีลเสมอ"
        sex = Person.Sex.MALE
        birthDate = LocalDate.now().minusYears(18)
        chronics.add(Chronic(Icd10("sleep", "I12")))
        chronics.add(Chronic(Icd10("god", "I15")))
        link = Link(System.JHICS)
        link!!.isSynced = false
        houseId = "99887744903"
    }

    @Before
    fun initDb() {

        daoPerson = MongoPersonDao()
        dao = MongoRelationsShipDao()

        somChai = daoPerson.insert(ORG_ID, `สมชาย`)
        somYing = daoPerson.insert(ORG_ID, `สมหญิง`)
        rabbit = daoPerson.insert(ORG_ID, `กระต่าย`)

        katar = daoPerson.insert(ORG_ID2, `กระแต`)
        karog = daoPerson.insert(ORG_ID2, `กระรอก`)

        somChai.relationships.add(Person.Relationship(Person.Relate.Child, somYing))
        somYing.relationships.add(Person.Relationship(Person.Relate.Father, somChai))
        daoPerson.update(ORG_ID, somChai)
        daoPerson.update(ORG_ID, somYing)

        katar.addRelationship(Person.Relate.Married to karog)
        karog.addRelationship(Person.Relate.Married to katar)
        daoPerson.update(ORG_ID2, katar)
        daoPerson.update(ORG_ID2, karog)
    }

    @Test
    fun get() {
        val dogRelation = dao.get(ORG_ID, somChai.id).first()
        val dogRelation2 = dao.get(ORG_ID2, katar.id).first()

        dogRelation.id `should be equal to` somYing.id
        dogRelation.relate `should equal` Person.Relate.Child
        dogRelation2.id `should be equal to` karog.id
        dogRelation2.relate `should equal` Person.Relate.Married
    }

    @Test
    fun update() {
        somChai.update {
            relationships.add(Person.Relationship(Person.Relate.Child, rabbit))
        }
        val dogUpdate = dao.update(ORG_ID, somChai.id, somChai.relationships)

        rabbit.update {
            relationships.add(Person.Relationship(Person.Relate.Father, somChai))
        }
        val rabbitUpdate = dao.update(ORG_ID, rabbit.id, rabbit.relationships)

        dogUpdate.first().id `should be equal to` somYing.id
        dogUpdate.last().id `should be equal to` rabbit.id

        rabbitUpdate.first().id `should be equal to` somChai.id
        rabbitUpdate.last().id `should be equal to` somChai.id
    }

    /**
     * สมชาย > สมหญิง > กระต่าย
     */
    @Test
    fun collectGenogram() {
        somYing.update {
            addRelationship(Pair(Person.Relate.Child, rabbit))
        }
        daoPerson.update(ORG_ID, somYing)

        rabbit.update {
            addRelationship(Pair(Person.Relate.Mother, somYing))
        }
        daoPerson.update(ORG_ID, rabbit)
        val rela = dao.collectGenogram(ORG_ID, somChai.id)
        rela.count() `should be equal to` 3
    }

    @Test
    fun removeOrgIdAndUpdate() {
        dao.get(ORG_ID, somChai.id).isNotEmpty() `should be equal to` true
        dao.removeByOrgId(ORG_ID)
        dao.get(ORG_ID, somChai.id).isEmpty() `should be equal to` true
        dao.update(ORG_ID, somChai.id, somChai.relationships)
        dao.get(ORG_ID, somChai.id).isNotEmpty() `should be equal to` true
    }

    @Test
    fun insertBlock() {
        dao.removeByOrgId(ORG_ID)
        dao.removeByOrgId(ORG_ID2)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to สมชาย.relationships,
            somYing.id to `สมหญิง`.relationships,
            rabbit.id to `กระต่าย`.relationships
        )
        val inputBlock2: Map<String, List<Person.Relationship>> = mapOf(
            karog.id to `กระรอก`.relationships,
            katar.id to `กระแต`.relationships
        )

        val resultBlock = dao.addRelation(ORG_ID, 2, inputBlock)
        val resultBlock2 = dao.addRelation(ORG_ID2, 1, inputBlock2)

        resultBlock.size `should be equal to` 3
        resultBlock2.size `should be equal to` 2
    }

    @Test
    fun getBlock() {
        dao.removeByOrgId(ORG_ID)
        dao.removeByOrgId(ORG_ID2)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to สมชาย.relationships,
            somYing.id to `สมหญิง`.relationships,
            rabbit.id to `กระต่าย`.relationships
        )
        val inputBlock2: Map<String, List<Person.Relationship>> = mapOf(
            karog.id to `กระรอก`.relationships,
            katar.id to `กระแต`.relationships
        )
        dao.addRelation(ORG_ID, 2, inputBlock)
        dao.addRelation(ORG_ID2, 1, inputBlock2)

        val resultBlock = dao.getBlock(ORG_ID, 2)
        val resultBlock2 = dao.getBlock(ORG_ID2, 1)

        resultBlock.size `should be equal to` 3
        resultBlock2.size `should be equal to` 2
    }

    @Test
    fun unConfirmBlock() {
        dao.removeByOrgId(ORG_ID)
        dao.removeByOrgId(ORG_ID2)

        val inputBlock: Map<String, List<Person.Relationship>> = mapOf(
            somChai.id to สมชาย.relationships,
            somYing.id to `สมหญิง`.relationships,
            rabbit.id to `กระต่าย`.relationships
        )
        val inputBlock2: Map<String, List<Person.Relationship>> = mapOf(
            karog.id to `กระรอก`.relationships,
            katar.id to `กระแต`.relationships
        )
        dao.addRelation(ORG_ID, 2, inputBlock)
        dao.addRelation(ORG_ID2, 1, inputBlock2)
        dao.unConfirmBlock(ORG_ID, 2)
        dao.unConfirmBlock(ORG_ID2, 1)

        val resultBlock = dao.getBlock(ORG_ID, 2)
        val resultBlock2 = dao.getBlock(ORG_ID2, 1)
        resultBlock.size `should be equal to` 0
        resultBlock2.size `should be equal to` 0

        dao.get(ORG_ID, somChai.id).isEmpty() `should be equal to` true
        dao.get(ORG_ID2, katar.id).isEmpty() `should be equal to` true
    }
}
