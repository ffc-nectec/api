package ffc.airsync.api.dao

import ffc.airsync.api.buildInsertBson
import ffc.airsync.api.ffcInsert
import ffc.entity.Person
import ffc.entity.gson.parseTo
import org.bson.Document
import org.bson.types.BasicBSONList
import java.util.ArrayList

internal class MongoPersonDao(host: String, port: Int) : PersonDao, MongoAbsConnect(host, port, "ffc", "person") {
    override fun insert(orgId: String, person: Person): Person {
        val personDoc = person.buildInsertBson()
        personDoc.append("orgId", orgId)
        personDoc.append("houseId", person.bundle["houseId"])

        return dbCollection.ffcInsert(personDoc)
    }

    override fun insert(orgId: String, persons: List<Person>): List<Person> {
        return persons.map { insert(orgId, it) }
    }

    override fun findByOrgId(orgId: String): List<Person> {
        val query = Document("orgId", orgId)
        val docPersonList = dbCollection.find(query)

        return docPersonList.map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun getPeopleInHouse(houseId: String): ArrayList<Person>? {
        val personInHouse = arrayListOf<Person>()
        val query = Document("houseId", houseId)
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

    override fun find(query: String, orgId: String): List<Person> {
        return findMongo(query, orgId)
    }

    private fun findMongo(query: String, orgId: String): List<Person> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val queryTextCondition = BasicBSONList().apply {
            val rex = Regex("""^ *\d+.*${'$'}""")

            if (rex.matches(query)) {
                add(Document("identities.id", regexQuery))
            } else {
                add(Document("firstname", regexQuery))
                add(Document("lastname", regexQuery))
            }
        }
        val queryTextReg = Document("\$or", queryTextCondition)
        val queryFixOrgIdDoc = Document("orgId", orgId)
        val fullQuery = BasicBSONList().apply {
            add(queryFixOrgIdDoc)
            add(queryTextReg)
        }
        val resultQuery = dbCollection.find(Document("\$and", fullQuery)).limit(20)

        return resultQuery.map {
            it.toJson().parseTo<Person>()
        }.toList()
    }

    override fun findByICD10(orgId: String, icd10: String): List<Person> {
        val query = Document("chronics.disease.icd10", icd10)
        val result = dbCollection.find(query).limit(20)

        return result.map { it.toJson().parseTo<Person>() }.toList()
    }
}
