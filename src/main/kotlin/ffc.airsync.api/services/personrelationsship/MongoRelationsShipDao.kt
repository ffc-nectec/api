package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.person.persons
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.plus
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.validate
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

class MongoRelationsShipDao(host: String, port: Int) : MongoAbsConnect(host, port, "ffc", "person"), GenoGramDao {
    /**
     * ค้นหาข้อมูล genogram จาก person โดยจะดึงมาเฉพาะ field relation
     * @param personId รหัส id Person.id
     * @param orgId Organization.id
     *
     */
    override fun get(orgId: String, personId: String): List<Person.Relationship> {
        val find = dbCollection.find("_id" equal ObjectId(personId)).projection(("relationships" equal 1) plus ("orgId" equal 1)).first()
            ?: throw NoSuchElementException("ไม่พบข้อมูล คนที่ระบุ")

        if (find["orgId"].toString() != orgId) throw NoSuchElementException("ไม่พบข้อมูล คนที่ระบุ")

        return find["relationships"]?.toJson()?.parseTo()
            ?: throw NoSuchElementException("ไม่พบข้อมูล ความสัมพันธ์ของบุคคลนี้")
    }

    override fun update(orgId: String, personId: String, relation: List<Person.Relationship>): List<Person.Relationship> {
        relation.validate(personId)
        get(orgId, personId)
        val relationDoc = "relationships" equal BasicBSONList().apply {
            relation.forEach {
                add(it.toDocument())
            }
        }

        dbCollection.updateOne("_id" equal ObjectId(personId), "\$set" equal relationDoc)

        return get(orgId, personId)
    }

    override fun collectGenogram(orgId: String, personId: String): List<Person> {
        return collectPerson(orgId, personId).map { it.value }
    }

    private fun collectPerson(orgId: String, personId: String, collect: HashMap<String, Person> = hashMapOf()): HashMap<String, Person> {
        val relation: List<Person.Relationship> = get(orgId, personId)

        if (collect[personId] == null)
            collect[personId] = persons.getPerson(orgId, personId)

        relation.forEach { personRelation ->
            if (personRelation.id.isNotBlank() && collect[personRelation.id] == null) {
                val person = persons.getPerson(orgId, personRelation.id)
                collect[personRelation.id] = person
                collectPerson(orgId, personRelation.id, collect)
            }
        }
        return collect
    }
}
