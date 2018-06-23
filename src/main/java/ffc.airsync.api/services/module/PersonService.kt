package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import ffc.entity.Person

object PersonService {

    fun get(orgId: String, page: Int, per_page: Int): List<Person> {
        //val tokenObj = getOrgByMobileToken(UUID.fromString(token.trim()), orgId)
        val org = orgDao.findById(orgId)
        val personList = personDao.find(org.uuid)
        printDebug("Person Service get list ${personList.size}")
        val personReturn = arrayListOf<Person>()


        val count = personList.size

        itemRenderPerPage(page, per_page, count, object : AddItmeAction {
            override fun onAddItemAction(itemIndex: Int) {

                val person = personList[itemIndex].data


                if (person.houseId != null) {
                    val housePerson = houseDao.findByHouseId(org.uuid, person.houseId!!)
                    printDebug("\thouse person $housePerson")
                    person.house = housePerson?.data
                }

                personReturn.add(person)

            }
        })

        return personReturn
    }


    fun create(orgId: String, personList: List<Person>) {
        val org = orgDao.findById(orgId)
        personDao.insert(org.uuid, personList)
    }
}
