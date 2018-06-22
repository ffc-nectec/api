package ffc.airsync.api.dao

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import ffc.airsync.api.printDebug
import ffc.entity.*
import org.joda.time.LocalDate
import java.util.*

class MongoPersonDao(host: String, port: Int, databaseName: String, collection: String) : PersonDao, MongoAbsConnect(host, port, databaseName, collection) {

    override fun insert(orgUUID: UUID, person: Person) {

        val query = BasicDBObject("orgUuid", orgUUID.toString())
                .append("pid", person.pid)
        coll.remove(query)

        val doc = objToDoc(person, orgUUID)
        coll.insert(doc)
    }

    override fun insert(orgUUID: UUID, personList: List<Person>) {
        personList.forEach {
            insert(orgUUID, it)
        }
    }

    override fun find(orgUuid: UUID): List<StorageOrg<Person>> {
        //printDebug("Mongo find persons in org $orgUuid")
        val personList = arrayListOf<StorageOrg<Person>>()

        val query = BasicDBObject("orgUuid", orgUuid.toString())
        val docPersonList = coll.find(query)

        printDebug("\tPerson in list ${docPersonList.size()}")
        while (docPersonList.hasNext()) {
            val it = docPersonList.next()
            val person = docToObj(it)
            personList.add(StorageOrg(data = person,
                    uuid = orgUuid))

        }


        printDebug("Person find finish list size ${personList.size}")
        return personList
    }


    override fun getPeopleInHouse(orgUUID: UUID, houseId: Int): ArrayList<People>? {
        val personInHouse = arrayListOf<People>()

        val query = BasicDBObject("orgUuid", orgUUID.toString())
                .append("houseId", houseId)


        val personInHouseDoc = coll.find(query)

        while (personInHouseDoc.hasNext()) {
            val personDoc = personInHouseDoc.next()
            val person = docToObj(personDoc)
            val people = People(person.pid.toString(), "${person.prename} ${person.firstname} ${person.lastname}")
            personInHouse.add(people)
        }


        return personInHouse
    }

    override fun removeGroupByOrg(orgUUID: UUID) {
        val query = BasicDBObject("orgUuid", orgUUID.toString())
        coll.remove(query)
    }

    private fun objToDoc(person: Person, orgUUID: UUID): DBObject {

        /*
        printDebug("\tPerson obj to mongo doc.")
        printDebug("\t\tid=${person.id}")
        printDebug("\t\torgId=${person.orgId}")
        printDebug("\t\thospCode=${person.hospCode}")
        printDebug("\t\tpid=${person.pid}")
        printDebug("\t\tprename=${person.prename}")
        printDebug("\t\tfirstname=${person.firstname}")
        printDebug("\t\tlastname=${person.lastname}")
        printDebug("\t\tbirthData=${person.birthData}")
        printDebug("\t\tidentities=${person.identities}")
        printDebug("\t\tchronics=${person.chronics}")
        printDebug("\t\thouseId=${person.houseId}")

*/
        var i = 1
        //printDebug("\t\t\t${i++}")
        val personDoc = BasicDBObject("id", person.id)
        //printDebug("\t\t\t${i++}${orgUUID}")
        personDoc.append("orgUuid", orgUUID.toString())
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("orgId", person.orgId)
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("hospCode", person.hospCode)
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("pid", person.pid)
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("prename", person.prename)
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("firstname", person.firstname)
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("lastname", person.lastname)
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        personDoc.append("birthData", person.birthDate.toString())


        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        //personDoc.append("identities", person.identities.toJson())
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")
        if (person.chronics != null) {
            personDoc.append("chronics", person.chronics!!.toJson())
        }
        //printDebug("\t\t\t${i++}${personDoc.toJson()}")


        personDoc.append("houseId", person.houseId)
        //printDebug("\t\tReturn doc ${personDoc.toJson()}")


        return personDoc
    }

    private fun docToObj(obj: DBObject): Person {
        //printDebug("\tMongo to person obj $obj.")

        //printDebug("\t\tid=${(obj.get("id") ?: null).toString()}")
        val person = Person(id = (obj.get("id") ?: null).toString().toLong())


        val orgIdObj = obj.get("orgId")
        if (orgIdObj != null) {
            //printDebug("		orgId=$1Obj")
            person.orgId = orgIdObj.toString().toInt()
        }
        val hospCodeObj = obj.get("hospCode")
        if (hospCodeObj != null) {
            //printDebug("		hospCode=$1Obj")
            person.hospCode = hospCodeObj.toString()
        }
        val pidObj = obj.get("pid")
        if (pidObj != null) {
            //printDebug("		pid=$1Obj")
            person.pid = pidObj.toString().toLong()
        }
        val prenameObj = obj.get("prename")
        if (prenameObj != null) {
            //printDebug("		prename=$1Obj")
            person.prename = prenameObj.toString()
        }
        val firstnameObj = obj.get("firstname")
        if (firstnameObj != null) {
            //printDebug("		firstname=$1Obj")
            person.firstname = firstnameObj.toString()
        }
        val lastnameObj = obj.get("lastname")
        if (lastnameObj != null) {
            //printDebug("		lastname=$1Obj")
            person.lastname = lastnameObj.toString()
        }
        val birthDataObj = obj.get("birthData")
        if (birthDataObj != null) {
            //printDebug("		birthData=$1Obj")
            person.birthDate = LocalDate.parse(birthDataObj.toString())
        }


        val identitiesObj = obj.get("identities")
        if (identitiesObj != null) {
            printDebug("		identities=$1Obj")
            person.identities = identitiesObj.toString().fromJson()
        }
        val chronicsObj = obj.get("chronics")
        if (chronicsObj != null) {
            printDebug("		chronics=$1Obj")
            person.chronics = chronicsObj.toString().fromJson()
        }


        val houseIdObj = obj.get("houseId")
        if (houseIdObj != null) {
            //printDebug("		houseId=$1Obj")
            person.houseId = houseIdObj.toString().toInt()
        }

        return person
    }
}
