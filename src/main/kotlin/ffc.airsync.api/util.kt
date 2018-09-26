/*
 * Copyright (c) 2018 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.airsync.api

import com.fatboyindustrial.gsonjodatime.DateTimeConverter
import com.fatboyindustrial.gsonjodatime.LocalDateConverter
import com.fatboyindustrial.gsonjodatime.LocalDateTimeConverter
import com.google.gson.GsonBuilder
import com.mongodb.client.MongoCollection
import ffc.entity.Entity
import ffc.entity.Identity
import ffc.entity.Lang
import ffc.entity.User
import ffc.entity.copy
import ffc.entity.gson.HealthCareJsonAdapter
import ffc.entity.gson.IdentityJsonAdapter
import ffc.entity.gson.UserJsonAdapter
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import ffc.entity.healthcare.HealthCareService
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Geometry
import me.piruin.geok.gson.GeometrySerializer
import me.piruin.geok.gson.LatLngSerializer
import me.piruin.geok.gson.adapterFor
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone
import javax.ws.rs.ForbiddenException

val debug = System.getenv("FFC_DEBUG")
fun <T> printDebug(infoDebug: T) {
    if (debug == null) println(infoDebug)
}

val DATETIMEBANGKOK: DateTime get() = DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Bangkok"))))
val TIMESTAMPBANGKOK: Timestamp get() = Timestamp(DATETIMEBANGKOK.plusHours(7).millis)

inline fun <reified T> getResourceAs(filename: String): T {
    val classloader = Thread.currentThread().contextClassLoader
    val file = classloader.getResourceAsStream(filename)
        .bufferedReader(Charset.forName("UTF-8"))

    return file.readText().parseTo()
}

fun Locale.toLang(): Lang {
    return when (language) {
        "th" -> Lang.th
        else -> Lang.en
    }
}

fun Entity.buildInsertBson(): Document {
    if (!isTempId) throw ForbiddenException("ข้อมูล $type ที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ $id : isTempId = $isTempId")
    val generateId = ObjectId()
    val insertObj = copy(generateId.toHexString().trim())
    return insertObj.buildBsonDoc()
}

fun Entity.buildUpdateBson(oldDoc: Document): Document {
    if (isTempId) throw ForbiddenException("ข้อมูล $type ที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ $id : isTempId = $isTempId")
    val oldBundle = oldDoc.toJson().parseTo<Entity>(airSyncGson).bundle
    this.bundle.clear()
    this.bundle.putAll(oldBundle)
    return this.buildBsonDoc()
}

private fun Entity.buildBsonDoc(): Document {
    val generateId = ObjectId(id)
    val json = toJson(airSyncGson)
    val doc = Document.parse(json)
    doc.append("_id", generateId)

    return doc
}

inline fun <reified T> MongoCollection<Document>.ffcInsert(doc: Document): T {
    require(doc["_id"] != null) { "ต้องมี _id ในขั้นตอนการ Insert" }
    insertOne(doc)
    val query = Document("_id", doc["_id"] as ObjectId)
    val result = find(query).first()

    return result.toJson().parseTo()
}

val airSyncGson = GsonBuilder()
    .adapterFor<User>(UserJsonAdapter())
    .adapterFor<Identity>(IdentityJsonAdapter())
    .adapterFor<HealthCareService>(HealthCareJsonAdapter())
    .adapterForExtLibrary()
    .create()

private fun GsonBuilder.adapterForExtLibrary(): GsonBuilder {
    adapterFor<Geometry>(GeometrySerializer())
    adapterFor<LatLng>(LatLngSerializer())
    adapterFor<DateTime>(DateTimeConverter())
    adapterFor<LocalDate>(LocalDateConverter())
    adapterFor<LocalDateTime>(LocalDateTimeConverter())
    return this
}
