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

import com.mongodb.client.MongoCollection
import ffc.entity.Entity
import ffc.entity.Lang
import ffc.entity.copy
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.nio.charset.Charset
import java.util.Locale
import javax.ws.rs.ForbiddenException

val debug = System.getenv("FFC_DEBUG")
fun <T> printDebug(infoDebug: T) {
    if (debug == null) println(infoDebug)
}

val DATETIMEBANGKOK = lazy { DateTime(DateTimeZone.UTC).plusHours(7)!! }.value

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

fun Entity.buildUpdateBson(): Document {
    if (isTempId) throw ForbiddenException("ข้อมูล $type ที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ $id : isTempId = $isTempId")

    return this.buildBsonDoc()
}

private fun Entity.buildBsonDoc(): Document {
    val generateId = ObjectId(id)
    val json = toJson()
    val doc = Document.parse(json)
    doc.append("_id", generateId)

    return doc
}

inline fun <reified T> MongoCollection<Document>.ffcInsert(doc: Document): T {
    insertOne(doc)
    val query = Document("_id", doc["_id"] as ObjectId)
    val result = find(query).first()

    return result.toJson().parseTo()
}
