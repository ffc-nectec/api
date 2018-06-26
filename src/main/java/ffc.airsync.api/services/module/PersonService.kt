package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.Person

object PersonService {

    fun get(orgId: String, page: Int, per_page: Int): List<Person> {
        //val tokenObj = getOrgByMobileToken(UUID.fromString(token.trim()), orgId)
        //val org = orgDao.find(orgId)
        val personList = personDao.findByOrgId(orgId)
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


    fun create(orgId: String, personList: List<Person>) {
        personDao.insert(orgId, personList)
    }
}
