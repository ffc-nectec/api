package ffc.airsync.api.services.personrelationsship

import ffc.airsync.api.services.MongoAbsConnect
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.plus
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
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

    override fun update(orgId: String, personId: String, relation: List<Person.Relationship>, validate: (personId: String, updateRelation: List<Person.Relationship>) -> Unit): List<Person.Relationship> {
        get(orgId, personId)
        validate(personId, relation)
        val relationDoc = "relationships" equal BasicBSONList().apply {
            relation.forEach {
                add(it.toDocument())
            }
        }

        dbCollection.updateOne("_id" equal ObjectId(personId), "\$set" equal relationDoc)

        return get(orgId, personId)
    }
}
