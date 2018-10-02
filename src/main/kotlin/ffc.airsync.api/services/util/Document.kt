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

package ffc.airsync.api.services.util

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import ffc.airsync.api.airSyncGson
import ffc.airsync.api.security.password
import ffc.entity.Entity
import ffc.entity.User
import ffc.entity.copy
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import javax.ws.rs.ForbiddenException
import kotlin.collections.map as mapKt

inline fun <reified T> FindIterable<Document>.firstAs(): T = first().toJson().parseTo<T>()

inline fun <reified T> FindIterable<Document>.listOf(): List<T> = mapKt { it.toJson().parseTo<T>() }

fun bsonListOf(vararg document: Document): BasicBSONList = BasicBSONList().apply { document.forEach { add(it) } }

fun documentOf(vararg pair: Pair<String, Any?>): Document = Document(pair.toMap())

fun Any.toDocument(): Document = Document.parse(toJson())

infix fun String.equal(param: Any?): Document = Document(this, param)

internal infix fun Document.plus(doc: Document): Document {
    doc.forEach { key, value ->
        this.append(key, value)
    }
    return this
}

inline fun <reified T : Entity> MongoCollection<Document>.insert(entity: T, vararg pair: Pair<String, Any?>): T {
    val doc = entity.buildInsertBson().apply { putAll(pair) }
    insertOne(doc)
    return find("_id" equal doc["_id"] as ObjectId).firstAs()
}

fun Entity.buildInsertBson(): Document {
    if (!isTempId) throw ForbiddenException("ข้อมูล $type ที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ $id : isTempId = $isTempId")
    val generateId = ObjectId()
    val insertObj = copy(newId = generateId.toHexString().trim())
    return insertObj.buildBsonDoc()
}

fun Entity.buildUpdateBson(oldDoc: Document): Document {
    if (isTempId) throw ForbiddenException("ข้อมูล $type ที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ $id : isTempId = $isTempId")
    val oldBundle = oldDoc.toJson().parseTo<Entity>(airSyncGson).bundle
    this.bundle.clear()
    this.bundle.putAll(oldBundle)
    return this.buildBsonDoc()
}

fun Entity.buildBsonDoc(): Document {
    val generateId = ObjectId(id)
    val json = toJson(airSyncGson)
    val doc = Document.parse(json)
    doc.append("_id", generateId)

    return doc
}

internal inline fun <reified T> MongoCollection<Document>.ffcInsert(doc: Document): T {
    require(doc["_id"] != null) { "ต้องมี _id ในขั้นตอนการ Insert" }
    insertOne(doc)
    val query = Document("_id", doc["_id"] as ObjectId)

    return find(query).firstAs()
}

fun User.toDocument(): Document {
    val user = this.copy(ObjectId().toHexString())
    val document = Document.parse(user.toJson())
    document.append("password", password().hash(user.password))
    return document
}