package ffc.airsync.api

import ffc.entity.Link
import ffc.entity.Person
import ffc.entity.System
import ffc.entity.ThaiCitizenId
import ffc.entity.update
import ffc.entity.util.generateTempId
import org.amshove.kluent.shouldContain
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
        val doc = person.buildInsertBson()

        doc.toJson() shouldContain "1154801544875"
    }
}
