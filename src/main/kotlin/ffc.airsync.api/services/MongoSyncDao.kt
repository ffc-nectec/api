package ffc.airsync.api.services

import com.mongodb.BasicDBObject
import com.mongodb.client.model.UpdateOptions
import ffc.airsync.api.services.healthcareservice.healthCareServices
import ffc.airsync.api.services.healthcareservice.visitInsertDocument
import ffc.airsync.api.services.util.buildInsertBson
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.ffcInsert
import ffc.airsync.api.services.util.plus
import ffc.entity.Entity
import ffc.entity.Person
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.CommunityService
import ffc.entity.healthcare.HealthCareService
import ffc.entity.healthcare.HomeVisit
import ffc.entity.healthcare.NCDScreen
import ffc.entity.healthcare.SpecialPP
import ffc.entity.place.House
import org.bson.BsonDateTime
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId

private const val HEALTHCARETYPE = "HealthCareService"
private const val PERSONTYPE = "Person"
private const val HOUSETYPE = "House"
private const val COMMUNITYSERVICETYPE = "CommunityService"
private const val HOMEVISITTYPE = "HomeVisit"
private const val SPECIALPPTYPE = "SpecialPP"
private const val NCDSCREENTYPE = "NCDScreen"

abstract class MongoSyncDao<T : Entity>(dbName: String, collection: String) :
    MongoDao(dbName, collection), Sync<T> {

    override fun insertBlock(orgId: String, block: Int, item: List<T>): List<T> {
        val itemInsert = item.map {
            val itemDoc = when (it.type) {
                HEALTHCARETYPE -> healthCareServices.visitInsertDocument(it as HealthCareService, orgId)
                PERSONTYPE -> {
                    val personDoc = it.buildInsertBson()
                    (it as Person).birthDate?.toDate()?.time?.let { time ->
                        personDoc.append("birthDateMongo", BsonDateTime(time))
                    }
                    personDoc
                }
                else -> it.buildInsertBson()
            }
            itemDoc["orgIndex"] = ObjectId(orgId)
            itemDoc["insertBlock"] = block
            itemDoc["orgId"] = orgId
            itemDoc
        }

        return when (item.first().type!!.toString()) {
            PERSONTYPE -> dbCollection.ffcInsert<Person>(itemInsert) as List<T>
            HOUSETYPE -> dbCollection.ffcInsert<House>(itemInsert) as List<T>
            HEALTHCARETYPE -> dbCollection.ffcInsert<HealthCareService>(itemInsert) as List<T>
            COMMUNITYSERVICETYPE -> dbCollection.ffcInsert<CommunityService>(itemInsert) as List<T>
            HOMEVISITTYPE -> dbCollection.ffcInsert<HomeVisit>(itemInsert) as List<T>
            SPECIALPPTYPE -> dbCollection.ffcInsert<SpecialPP>(itemInsert) as List<T>
            NCDSCREENTYPE -> dbCollection.ffcInsert<NCDScreen>(itemInsert) as List<T>
            else -> emptyList()
        }
    }

    override fun getBlock(orgId: String, block: Int): List<T> {
        val result = dbCollection.find("insertBlock" equal block)

        try {
            return when (result.first()["type"]?.toString()) {
                PERSONTYPE -> result.map { it.toJson().parseTo<Person>() }.toList() as List<T>
                HOUSETYPE -> result.map { it.toJson().parseTo<House>() }.toList() as List<T>
                HEALTHCARETYPE -> result.map {
                    it.toJson().parseTo<HealthCareService>()
                }.toList() as List<T>
                COMMUNITYSERVICETYPE -> result.map {
                    it.toJson().parseTo<CommunityService>()
                }.toList() as List<T>
                HOMEVISITTYPE -> result.map { it.toJson().parseTo<HomeVisit>() }.toList() as List<T>
                SPECIALPPTYPE -> result.map { it.toJson().parseTo<SpecialPP>() }.toList() as List<T>
                NCDSCREENTYPE -> result.map { it.toJson().parseTo<NCDScreen>() }.toList() as List<T>
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

        dbCollection.updateMany(
            ("insertBlock" equal block) plus ("orgIndex" equal ObjectId(orgId)),
            update,
            UpdateOptions()
        )
    }

    override fun unConfirmBlock(orgId: String, block: Int) {
        dbCollection.deleteMany(("insertBlock" equal block) plus ("orgIndex" equal ObjectId(orgId)))
    }

    private fun String.className() = Regex(""".*\.([\w\d]+)""").matchEntire(this)?.groupValues?.last().toString()
    private inline fun <reified T> className() = T::class.toString().className()
}
