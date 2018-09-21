package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.Person

object PersonService {
    fun get(orgId: String, page: Int, per_page: Int): List<Person> {

        val personList = persons.findByOrgId(orgId)
        printDebug("Person Service get list ${personList.size}")
        val personReturn = arrayListOf<Person>()
        val count = personList.size

        itemRenderPerPage(page, per_page, count, object : AddItmeAction {
            override fun onAddItemAction(itemIndex: Int) {
                val person = personList[itemIndex]
                personReturn.add(person)
            }
        })

        return personReturn
    }

    fun create(orgId: String, personList: List<Person>): List<Person> {
        return persons.insert(orgId, personList)
    }

    fun find(orgId: String, query: String): List<Person> {
        return persons.find(orgId = orgId, query = query)
    }

    fun findICD10(orgId: String, icd10: String): List<Person> {
        return persons.findByICD10(orgId, icd10)
    }
}
