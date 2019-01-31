package ffc.airsync.api.services.specialpp

import ffc.airsync.api.MongoDbTestRule
import ffc.entity.healthcare.SpecialPP
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MongoSpecialPpTypeTest {

    @JvmField
    @Rule
    val mongo = MongoDbTestRule()

    lateinit var dao: SpecialPpDao

    val ppType = SpecialPP.PPType("I3234", "เยี่ยมเบาตัว")

    @Before
    fun setUp() {
        dao = MongoSpecialPpType()
        dao.insert(ppType)
    }

    @Test
    fun get() {
        dao.get("I3234").name `should be equal to` "เยี่ยมเบาตัว"
    }

    @Test(expected = NoSuchElementException::class)
    fun getFall() {
        dao.get("xxx")
    }

    @Test
    fun query() {
        dao.query("323").first().name `should be equal to` "เยี่ยมเบาตัว"
    }

    @Test
    fun queryFall() {
        dao.query("sadf").size `should be equal to` 0
    }
}
