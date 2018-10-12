package ffc.airsync.api.services.person

import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.plus
import ffc.entity.Entity
import ffc.entity.Person
import ffc.entity.gson.parseTo
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

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
        personDoc.append("orgId", ObjectId(orgId))

        return dbCollection.ffcInsert(personDoc)
    }

    override fun update(orgId: String, person: Person): Person {
        val query = "_id" equal ObjectId(person.id)
        val personOldDoc = dbCollection.find(query).first()

        check(personOldDoc["orgId"] == ObjectId(orgId)) { "ไม่พบคน" }
        val personDoc = person.buildUpdateBson(personOldDoc)

        personDoc.append("orgId", ObjectId(orgId))

        dbCollection.replaceOne(query, personDoc)

        return (dbCollection.find(query).first()
            ?: throw javax.ws.rs.InternalServerErrorException("ไม่สามารถค้นหาข้อมูลคนหลัง Update ได้ โปรติดต่อ Admin")).toJson().parseTo()
    }

    override fun getPerson(orgId: String, personId: String): Person {
        val query = ("orgId" equal ObjectId(orgId)) plus ("_id" equal ObjectId(personId))
        val result = dbCollection.find(query).first()
            ?: throw NullPointerException("ไม่พบรหัส person id $personId ที่ค้นหา")
        return result.toJson().parseTo()
    }

    override fun insert(orgId: String, persons: List<Person>): List<Person> {
        return persons.map { insert(orgId, it) }
    }

    override fun findByOrgId(orgId: String): List<Person> {
        return dbCollection.find("orgId" equal ObjectId(orgId))
            .map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun getPeopleInHouse(orgId: String, houseId: String): List<Person> {
        return dbCollection.find(("houseId" equal houseId) plus ("orgId" equal ObjectId(orgId)))
            .map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun remove(orgId: String) {
        dbCollection.deleteMany("orgId" equal ObjectId(orgId))
    }

    override fun find(query: String, orgId: String): List<Person> {
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val queryTextCondition = BasicBSONList().apply {
            val rex = Regex("""^\d+$""")

            if (rex.matches(query)) {
                add("identities.id" equal regexQuery)
            } else {
                add("firstname" equal regexQuery)
                add("lastname" equal regexQuery)
            }
        }
        val queryTextReg = "\$or" equal queryTextCondition
        val queryFixOrgIdDoc = "orgId" equal ObjectId(orgId)
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

    override fun syncData(orgId: String, limitOutput: Int): List<Entity> {
        val result = this.dbExecuted.find(("link.isSynced" equal false) plus ("orgId" equal ObjectId(orgId))).limit(limitOutput)

        if (result.count() < 1) return emptyList()
        val output = result.map {
            try {
                it.toJson().parseTo<Entity>()
            } catch (ex: Exception) {
                Entity()
            }
        }.toMutableList()
        output.removeIf { it.isTempId }

        return output
    }
}
