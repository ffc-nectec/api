package ffc.airsync.api

import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.update
import ffc.entity.util.generateTempId
import org.amshove.kluent.`should be equal to`
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Test

class utilTest {
    val person = Person(generateTempId()).update(DateTime.parse("2018-06-25T14:09:07.815+07:00")) {
        identities.add(ThaiCitizenId("1154801544875"))
        prename = "นาย"
        firstname = "พิรุณ"
        lastname = "พานิชผล"
        birthDate = LocalDate.parse("1993-06-29")
        link = Link(
            System.JHICS, "pid" to "1234567", "cid" to "11014578451234",
            lastSync = DateTime.parse("2018-06-25T14:09:07.815+07:00")
        )
    }

    @Test
    fun entityBuildBsonDoc() {
        val doc = person.buildBsonDoc()

        doc.toJson() `should be equal to` """{ "_id" : { """" + """$""" + """oid" : "e079e175c75a44f180e8eaeb" }, "identities" : [{ "type" : "thailand-citizen-id", "id" : "1154801544875" }], "prename" : "นาย", "firstname" : "พ\u0e34ร\u0e38ณ", "lastname" : "พาน\u0e34ชผล", "sex" : "UNKNOWN", "birthDate" : "1993-06-29", "chronics" : [], "link" : { "isSynced" : true, "lastSync" : "2018-06-25T14:09:07.815+07:00", "system" : "JHICS", "keys" : { "pid" : "1234567", "cid" : "11014578451234" } }, "id" : "e079e175c75a44f180e8eaeb", "type" : "Person", "timestamp" : "2018-06-25T14:09:07.815+07:00" }"""
    }

    @Test
    fun checkCreateCondition() {
        val personInsert: Person = person.buildInsertObject()

        personInsert.isTempId `should be equal to` false
    }
}
