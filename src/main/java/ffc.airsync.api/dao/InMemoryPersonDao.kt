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

package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.People
import ffc.entity.Person
import ffc.entity.StorageOrg
import java.util.*
import javax.ws.rs.NotFoundException
import kotlin.collections.HashMap

class InMemoryPersonDao : PersonDao {

    private constructor()

    companion object {
        val instant = InMemoryPersonDao()
    }


    val personList: ArrayList<StorageOrg<Person>> = arrayListOf()


    val peopleHouseList = arrayListOf<StorageOrg<HashMap<Int, ArrayList<People>>>>() //คนในบ้าน


    override fun removeGroupByOrg(orgUUID: UUID) {
        peopleHouseList.removeIf {
            it.uuid == orgUUID
        }
        personList.removeIf {
            it.uuid == orgUUID
        }


    }


    override fun insert(orgUUID: UUID, person: Person) {
        personList.removeIf { it.uuid == orgUUID && it.data.pid == person.pid }
        personList.add(StorageOrg(orgUUID, person))
        peopleToHouse(orgUUID = orgUUID, person = person)
    }

    override fun insert(orgUUID: UUID, personList: List<Person>) {
        personList.forEach {
            insert(orgUUID, it)
        }
    }

    override fun find(orgUuid: UUID): List<StorageOrg<Person>> {
        val data = personList.filter { it.uuid == orgUuid }
        if (data.size < 1) throw NotFoundException()
        return data
    }

    private fun peopleToHouse(orgUUID: UUID, person: Person) {

        val houseId = person.houseId
        if (houseId == null) {
            return
        }

        var peopleInOrg = getPeopleInOrg(orgUUID)
        if (peopleInOrg == null) {
            peopleInOrg = HashMap()
            peopleHouseList.add(StorageOrg(orgUUID, peopleInOrg))
        }


        var peopleInHouse = peopleInOrg.get(houseId)
        if (peopleInHouse == null) {
            peopleInHouse = arrayListOf<People>()
            peopleInOrg.put(houseId, peopleInHouse)
        }


        val name = person.prename + " " + person.firstname + " " + person.lastname
        val cardId = person.identities[0].id
        val chronic = person.chronics
        val people = People(cardId, name, chronic)


        peopleInHouse.removeIf { it.id == cardId }
        peopleInHouse.add(people)


    }

    override fun getPeopleInHouse(orgUUID: UUID, houseId: Int): ArrayList<People>? {

        printDebug("GetPeopleInHouse InMemoryPersonDao orgUUID=$orgUUID houseId=$houseId")
        val peopleInOrg = getPeopleInOrg(orgUUID = orgUUID)




        if (peopleInOrg == null) {
            printDebug("\tPeople in org Null")
            return null
        }


        val peopleInHouse = peopleInOrg[houseId]


        if (peopleInHouse == null) {
            printDebug("\tHouse find null $houseId")
        }

        printDebug("\tPeople in house Id $houseId")
        printDebug("\t\tcount ${peopleInHouse?.size}")

        //peopleInHouse?.forEach { printDebug(it) }

        return peopleInHouse
    }

    private inline fun getPeopleInOrg(orgUUID: UUID): HashMap<Int, ArrayList<People>>? {
        val peopleInOrg = peopleHouseList.find { it.uuid == orgUUID }?.data //int เป็น id บ้าน
        return peopleInOrg

    }


}
