package ffc.airsync.api.dao

import ffc.airsync.api.printDebug
import ffc.entity.People
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import java.util.ArrayList

class MongoPersonDao(host: String, port: Int, databaseName: String, collection: String) : PersonDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insert(orgId: String, person: Person) {

        val personDoc = Document.parse(person.toJson())
        personDoc.append("orgId", person.bundle["orgId"])
        personDoc.append("houseId", person.bundle["houseId"])
        coll2.insertOne(personDoc)
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
        val docPersonList = coll2.find(query)

        printDebug("\tPerson in list ${docPersonList.count()}")
        docPersonList.forEach {
            // val person = docToObj(it)
            val person: Person = it.toJson().parseTo()
            personList.add(person)
        }

        printDebug("Person find finish list size ${personList.size}")
        return personList
    }
    override fun getPeopleInHouse(houseId: String): ArrayList<People>? {
        val personInHouse = arrayListOf<People>()

        val query = Document("_id", houseId)

        val personInHouseDoc = coll2.find(query)
        personInHouseDoc.forEach {
            val personDoc = it
            val person: Person = personDoc.toJson().parseTo()
            val people = People(person.id, "${person.prename} ${person.firstname} ${person.lastname}")
            personInHouse.add(people)
        }
        return personInHouse
    }

    override fun removeGroupByOrg(orgId: String) {
        val query = Document("orgId", orgId)
        coll2.deleteMany(query)
    }
}
