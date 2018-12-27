package ffc.airsync.api.services.person

import com.mongodb.client.FindIterable
import com.mongodb.client.model.IndexOptions
import ffc.airsync.api.services.MongoSyncDao
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.buildUpdateBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.firstAs
import ffc.airsync.api.services.util.plus
import ffc.entity.Entity
import ffc.entity.Person
import ffc.entity.gson.parseTo
import org.bson.BsonTimestamp
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

internal class MongoPersonDao(host: String, port: Int) : PersonDao, MongoSyncDao<Person>(host, port, "ffc", "person") {

    init {
        try {
            dbCollection.createIndex("orgIndex" equal 1, IndexOptions().unique(false))
            dbCollection.createIndex("houseId" equal 1, IndexOptions().unique(false))
            dbCollection.createIndex("relationships.block" equal 1, IndexOptions().unique(false))
            dbCollection.createIndex(("orgIndex" equal 1) plus ("_id" equal 1), IndexOptions().unique(true))
        } catch (ignore: Exception) {
        }
    }

    override fun insert(orgId: String, person: Person): Person {
        person.orgId = orgId
        val personDoc = person.buildInsertBson()
        personDoc.append("orgIndex", ObjectId(orgId))

        person.birthDate?.toInterval()?.toDurationMillis()?.let {
            personDoc.append("birthDateMongo", BsonTimestamp(it))
        }

        return dbCollection.ffcInsert(personDoc)
    }

    override fun insert(orgId: String, person: List<Person>): List<Person> {
        val personInsert = person.map {
            it.orgId = orgId
            val personDoc = it.buildInsertBson()
            personDoc.append("orgIndex", ObjectId(orgId))
            it.birthDate?.toInterval()?.toDurationMillis()?.let { time ->
                personDoc.append("birthDateMongo", BsonTimestamp(time))
            }
            personDoc
        }

        return dbCollection.ffcInsert(personInsert)
    }

    override fun update(orgId: String, person: Person): Person {
        person.orgId = orgId
        val query = "_id" equal ObjectId(person.id)
        val personOldDoc = dbCollection.find(query).first()

        check(personOldDoc["orgId"] == orgId) { "ไม่พบคน" }
        val personDoc = person.buildUpdateBson()

        personDoc.append("orgIndex", ObjectId(orgId))
        person.birthDate?.toInterval()?.toDurationMillis()?.let {
            personDoc.append("birthDateMongo", BsonTimestamp(it))
        }

        dbCollection.replaceOne(query, personDoc)

        return dbCollection.find(query).firstAs()
            ?: throw javax.ws.rs.InternalServerErrorException("ไม่สามารถค้นหาข้อมูลคนหลัง Update ได้ โปรติดต่อ Admin")
    }

    override fun getPerson(orgId: String, personId: String): Person {
        val query = "_id" equal ObjectId(personId)
        var result: Document = Document()

        try {
            result = dbCollection.find(query).first()
                ?: throw NullPointerException("ไม่พบรหัส person id $personId ที่ค้นหา")
        } catch (ex: java.lang.IllegalStateException) {
            if (ex.message?.contains("state should be: open") == true) {
            }
        }
        require(result["orgId"].toString() == orgId) { "ไม่พบรหัส person id $personId ที่ค้นหา" }

        (result["relationships"] as ArrayList<Document>).add("insertBlock" equal 12)

        (result["relationships"] as ArrayList<Document>).removeIf {
            it["insertBlock"] != null
        }

        return result.toJson().parseTo()
    }

    override fun findByOrgId(orgId: String): List<Person> {
        return dbCollection.find("orgIndex" equal ObjectId(orgId))
            .map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun getPeopleInHouse(orgId: String, houseId: String): List<Person> {
        return dbCollection.find(("houseId" equal houseId) plus ("orgIndex" equal ObjectId(orgId)))
            .map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun remove(orgId: String) {
        dbCollection.deleteMany("orgIndex" equal ObjectId(orgId))
    }

    override fun find(query: String, orgId: String): List<Person> {
        val regexStartWith = Document("\$regex", "^$query").append("\$options", "i")
        val qeryIsNumber = Regex("""^\d{4}\d*$""").matches(query)

        val result1 = mongoSearch(qeryIsNumber, regexStartWith, orgId)
            .map { it.toJson().parseTo<Person>() }.toList()

        if (result1.size < 50) {
            val output = arrayListOf<Person>()
            output.addAll(result1)
            val regexQuery = Document("\$regex", query).append("\$options", "i")
            val result2 = mongoSearch(qeryIsNumber, regexQuery, orgId)
                .map { it.toJson().parseTo<Person>() }.toList()
            result2.forEach { person ->
                if (result1.find { person.id == it.id } == null)
                    output.add(person)
            }

            return output
        } else {

            return result1
        }
    }

    private fun mongoSearch(
        qeryIsNumber: Boolean,
        regexSearch: Document?,
        orgId: String
    ): FindIterable<Document> {
        val queryTextCondition = BasicBSONList().apply {

            if (qeryIsNumber) {
                add("identities.id" equal regexSearch)
            } else {
                add("firstname" equal regexSearch)
                add("lastname" equal regexSearch)
            }
        }
        val queryTextReg = "\$or" equal queryTextCondition
        val queryFixOrgIdDoc = "orgIndex" equal ObjectId(orgId)
        val fullQuery = BasicBSONList().apply {
            add(queryFixOrgIdDoc)
            add(queryTextReg)
        }
        return dbCollection.find("\$and" equal fullQuery).limit(50)
    }

    override fun findHouseId(orgId: String, personId: String): String {
        return dbCollection
            .find(("_id" equal ObjectId(personId)) plus ("orgIndex" equal ObjectId(orgId)))
            .projection(("houseId" equal 1) plus ("orgId" equal 1))
            .firstOrNull()?.let {
                // require(it["orgId"].toString() == orgId) { "ไม่พบรหัส person id $personId ที่ค้นหา" }
                it["houseId"].toString()
            } ?: ""
    }

    override fun findByICD10(orgId: String, icd10: String): List<Person> {
        val result = dbCollection
            .find("chronics.disease.icd10" equal icd10).limit(20)

        return result.map { it.toJson().parseTo<Person>() }.toList()
    }

    override fun syncData(orgId: String, limitOutput: Int): List<Entity> {
        val result = dbCollection.find(
            ("link.isSynced" equal false)
                plus ("orgIndex" equal ObjectId(orgId))
        ).limit(limitOutput)

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
