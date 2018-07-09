package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import java.util.ArrayList

class MongoPersonDao(host: String, port: Int) : PersonDao, MongoAbsConnect(host, port, "ffc", "person") {

    override fun insert(orgId: String, person: Person) {

        val personDoc = Document.parse(person.toJson())
        personDoc.append("orgId", person.bundle["orgId"])
        personDoc.append("houseId", person.bundle["houseId"])
        dbCollection.insertOne(personDoc)
    }

    override fun insert(orgId: String, personList: List<Person>) {
        personList.forEach {
            insert(orgId, it)
        }
    }

    override fun findByOrgId(orgId: String): List<Person> {
        // printDebug("Mongo findAll persons in org $orgUuid")
        val personList = arrayListOf<Person>()

        val query = Document("orgId", orgId)
        val docPersonList = dbCollection.find(query)

        printDebug("\tPerson in list ${docPersonList.count()}")
        docPersonList.forEach {
            // val person = docToObj(it)
            val person: Person = it.toJson().parseTo()
            personList.add(person)
        }

        printDebug("Person find finish list size ${personList.size}")
        return personList
    }

    override fun getPeopleInHouse(houseId: String): ArrayList<Person>? {
        val personInHouse = arrayListOf<Person>()

        val query = Document("id", houseId)

        val personInHouseDoc = dbCollection.find(query)
        personInHouseDoc.forEach {
            val personDoc = it
            val person: Person = personDoc.toJson().parseTo()
            personInHouse.add(person)
        }
        return personInHouse
    }

    override fun removeGroupByOrg(orgId: String) {
        val query = Document("orgId", orgId)
        dbCollection.deleteMany(query)
    }
}
