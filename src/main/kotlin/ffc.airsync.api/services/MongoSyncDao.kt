package ffc.airsync.api.services

import com.mongodb.BasicDBObject
import com.mongodb.client.model.UpdateOptions
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.entity.Entity
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.CommunityService
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import ffc.entity.healthcare.NCDScreen
import ffc.entity.healthcare.SpecialPP
import ffc.entity.place.House
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

abstract class MongoSyncDao<T : Entity>(host: String, port: Int, dbName: String, collection: String) :
    MongoAbsConnect(host, port, dbName, collection), Sync<T> {

    override fun insertBlock(orgId: String, block: Int, item: List<T>): List<T> {
        val itemInsert = item.map {
            val itemDoc = it.buildInsertBson()
            itemDoc["orgIndex"] = ObjectId(orgId)
            itemDoc["insertBlock"] = block
            itemDoc["orgId"] = orgId
            itemDoc
        }

        return when (item.first().type?.toString()) {
            className<Person>() -> dbCollection.ffcInsert<Person>(itemInsert) as List<T>
            className<House>() -> dbCollection.ffcInsert<House>(itemInsert) as List<T>
            className<HealthCareService>() -> dbCollection.ffcInsert<HealthCareService>(itemInsert) as List<T>
            className<CommunityService>() -> dbCollection.ffcInsert<CommunityService>(itemInsert) as List<T>
            className<HomeVisit>() -> dbCollection.ffcInsert<HomeVisit>(itemInsert) as List<T>
            className<SpecialPP>() -> dbCollection.ffcInsert<SpecialPP>(itemInsert) as List<T>
            className<NCDScreen>() -> dbCollection.ffcInsert<NCDScreen>(itemInsert) as List<T>
            else -> emptyList()
        }
    }

    override fun getBlock(orgId: String, block: Int): List<T> {
        val result = dbCollection.find("insertBlock" equal block)

        try {
            return when (result.first()["type"]?.toString()) {
                className<Person>() -> result.map { it.toJson().parseTo<Person>() }.toList() as List<T>
                className<House>() -> result.map { it.toJson().parseTo<House>() }.toList() as List<T>
                className<HealthCareService>() -> result.map {
                    it.toJson().parseTo<HealthCareService>()
                }.toList() as List<T>
                className<CommunityService>() -> result.map {
                    it.toJson().parseTo<CommunityService>()
                }.toList() as List<T>
                className<HomeVisit>() -> result.map { it.toJson().parseTo<HomeVisit>() }.toList() as List<T>
                className<SpecialPP>() -> result.map { it.toJson().parseTo<SpecialPP>() }.toList() as List<T>
                className<NCDScreen>() -> result.map { it.toJson().parseTo<NCDScreen>() }.toList() as List<T>
                else -> emptyList()
            }
        } catch (ex: java.lang.NullPointerException) {
            return emptyList()
        }
    }

    override fun confirmBlock(orgId: String, block: Int) {
        val listUnset = BasicBSONList()
        listUnset.add("insertBlock" equal "")
        val update = BasicDBObject()
        update["\$unset"] = BasicDBObject("insertBlock", "")

        dbCollection.updateMany("insertBlock" equal block, update, UpdateOptions())
    }

    override fun unConfirmBlock(orgId: String, block: Int) {
        dbCollection.deleteMany("insertBlock" equal block)
    }

    private fun String.className() = Regex(""".*\.([\w\d]+)$""").matchEntire(this)?.groupValues?.last().toString()
    private inline fun <reified T> className() = T::class.toString().className()
}
