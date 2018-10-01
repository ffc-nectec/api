package ffc.airsync.api.dao

import com.mongodb.client.model.IndexOptions
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.gson.parseTo
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import javax.ws.rs.NotFoundException

internal class MongoPersonDao(host: String, port: Int) : PersonDao, MongoAbsConnect(host, port, "ffc", "person") {

    init {
        try {
            dbCollection.createIndex("orgId" equal 1, IndexOptions().unique(false))
            dbCollection.createIndex("houseId" equal 1, IndexOptions().unique(false))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(orgId: String, person: Person): Person {
        val personDoc = person.buildInsertBson()
        personDoc.append("orgId", orgId)

        if (person.link?.system == System.JHICS) {
            personDoc.append("houseId", person.link!!.keys["hcode"])
        }

        return dbCollection.ffcInsert(personDoc)
    }

    override fun getPerson(orgId: String, personId: String): Person {
        val query = ("orgId" equal orgId) plus ("_id" equal ObjectId(personId))
        val result = dbCollection.find(query).first()
            ?: throw NotFoundException("ไม่พบรหัส person id $personId ที่ค้นหา")
        return result.toJson().parseTo()
    }

    override fun insert(orgId: String, persons: List<Person>): List<Person> {
        return persons.map { insert(orgId, it) }
    }

    override fun findByOrgId(orgId: String): List<Person> {
        return dbCollection.find("orgId" equal orgId)
            .map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun getPeopleInHouse(orgId: String, houseId: String): List<Person> {
        return dbCollection.find(("houseId" equal houseId) plus ("orgId" equal orgId))
            .map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun remove(orgId: String) {
        dbCollection.deleteMany("orgId" equal orgId)
    }

    override fun find(query: String, orgId: String): List<Person> {
        return findMongo(query, orgId)
    }

    private fun findMongo(query: String, orgId: String): List<Person> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val queryTextCondition = BasicBSONList().apply {
            val rex = Regex("""^ *\d+.*${'$'}""")

            if (rex.matches(query)) {
                add("identities.id" equal regexQuery)
            } else {
                add("firstname" equal regexQuery)
                add("lastname" equal regexQuery)
            }
        }
        val queryTextReg = "\$or" equal queryTextCondition
        val queryFixOrgIdDoc = "orgId" equal orgId
        val fullQuery = BasicBSONList().apply {
            add(queryFixOrgIdDoc)
            add(queryTextReg)
        }
        val resultQuery = dbCollection.find("\$and" equal fullQuery).limit(20)

        return resultQuery.map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun findByICD10(orgId: String, icd10: String): List<Person> {
        val result = dbCollection
            .find("chronics.disease.icd10" equal icd10).limit(20)

        return result.map { it.toJson().parseTo<Person>() }.toList()
    }
}
