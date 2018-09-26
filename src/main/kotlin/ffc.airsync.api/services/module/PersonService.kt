package ffc.airsync.api.services.module

import ffc.entity.Person

object PersonService {
    fun get(orgId: String, page: Int, per_page: Int): List<Person> {
        return persons.findByOrgId(orgId).paging(page, per_page)
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

    fun create(orgId: String, person: Person): Person {
        return persons.insert(orgId, person)
    }
}
