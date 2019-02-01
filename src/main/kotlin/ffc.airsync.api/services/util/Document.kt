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

import com.mongodb.MongoBulkWriteException
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.InsertManyOptions
import ffc.airsync.api.security.password
import ffc.entity.Entity
import ffc.entity.User
import ffc.entity.copy
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.apache.logging.log4j.LogManager
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import javax.ws.rs.ForbiddenException
import kotlin.collections.List
import kotlin.collections.arrayListOf
import kotlin.collections.forEach
import kotlin.collections.putAll
import kotlin.collections.toMap
import kotlin.collections.map as mapKt

private val logger = LogManager.getLogger("ffc.airsync.api.services.util")

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

/**
 * ตัวช่วยสำหรับการสร้าง query จาก Entity
 * @return Document("_id", ObjectId(id))
 */
fun Entity.buildQueryDoc(): Document = "_id" equal ObjectId(id)

/**
 * ตัวช่วยสำหรับการสร้าง query จาก String
 * @return Document("_id", ObjectId(this))
 */
fun String.buildQueryDoc(): Document = "_id" equal ObjectId(this)

/**
 * build Entity ให้เป็น เอกสาร Insert
 * มีการตรวจสอบ TempId
 * สร้าง _id ขึ้นมา
 * ทำให้ข้อมูล id กับ _id ตรงกัน
 * @return เอกสาร bson doc สำหรับการ Insert
 */
fun Entity.buildInsertBson(): Document {
    if (!isTempId) throw ForbiddenException("ข้อมูล $type ที่ใส่ไม่ตรงตามเงื่อนไข ตรวจสอบ $id : isTempId = $isTempId")
    val generateId = ObjectId()
    val insertObj = copy(newId = generateId.toHexString().trim())
    return insertObj.buildBsonDoc()
}

/**
 * build Entity ให้เป็น เอกสาร Update
 * มีการตรวจสอบ TempId
 * ทำให้ข้อมูล id กับ _id ตรงกัน
 * คัดลอก้อมูล bundle จาก ที่เก่า มาใส่ในชุดข้อมูลที่ Update
 * @return เอกสาร bson doc สำหรับการ update
 */
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

internal inline fun <reified T> MongoCollection<Document>.ffcInsert(doc: Document): T {
    require(doc["_id"] != null) { "ต้องมี _id ในขั้นตอนการ Insert" }
    insertOne(doc)
    val query = Document("_id", doc["_id"] as ObjectId)

    return find(query).firstAs()
}

internal inline fun <reified T> MongoCollection<Document>.ffcUpdate(doc: Document): T {
    require(doc["_id"] != null) { "ต้องมี _id ในขั้นตอนการ Insert" }
    replaceOne("_id" equal doc["_id"], doc)
    val query = Document("_id", doc["_id"] as ObjectId)

    return find(query).firstAs()
}

internal inline fun <reified T> MongoCollection<Document>.ffcInsert(doc: List<Document>): List<T> {
    //val query = arrayListOf<Document>()
    doc.forEach {
        require(it["_id"] != null) { "ต้องมี _id ในขั้นตอนการ Insert" }
        //query.add("_id" equal it["_id"])
    }

    smartInsert(doc, 0)

    return doc.mapKt {
        val result = find("_id" equal ObjectId(it["_id"].toString())).first()!!
        result.remove("_id")
        result.toJson().parseTo<T>()
    }
}

private fun MongoCollection<Document>.smartInsert(doc: List<Document>, deep: Int = 0) {
    try {
        insertMany(doc, InsertManyOptions())
    } catch (ex: MongoBulkWriteException) {
        val size = doc.size
        if (doc.size > 1) {
            smartInsert(doc.subList(0, size / 2), deep + 1)
            smartInsert(doc.subList((size / 2) + 1, size), deep + 1)
        } else {
            logger.debug("mongo smart insert /2 error $deep")
            throw ex
        }
    }
}

fun User.toDocument(): Document {
    val user = this.copy(ObjectId().toHexString())
    val document = Document.parse(user.toJson())
    document.append("password", password().hash(user.password))
    return document
}

/**
 * fun สำหรับใช้งานกับ String.buildTextFindMongo
 */
typealias TextFindMongo = () -> List<String>

/**
 * สร้างเอกสาร bson Document สำหรับการ query
 * @param this ข้อความที่ใช้ ค้นหา
 * @param orgId orgId
 * @param queryStartWithNumber ถ้าข้อมูล query เริ่มต้นด้วย ตัวเลข จะให้ค้นหา field ไหนบ้าง ใช้เมื่อต้องการ Optimize
 * @param queryStartWithString ถ้าข้อมูล query เริ่มต้นด้วย ตัวอักษร จะให้ค้นหา field ไหนบ้าง ใช้เมื่อต้องการ Optimize
 * @param queryField จะให้ค้นหา field ไหนบ้าง โดยไม่สนว่า query จะเป็นตัวเลข หรือ ตัวอักษร
 *
 * @return bson Document ที่ใช้สำหรับการ query ใน mongo
 */
fun String.buildTextFindMongo(
    orgId: String,
    queryStartWithNumber: TextFindMongo = { arrayListOf() },
    queryStartWithString: TextFindMongo = { arrayListOf() },
    queryField: TextFindMongo = { arrayListOf() }
): Document {
    val regexQuery = Document("\$regex", this).append("\$options", "i")
    val queryTextCondition = BasicBSONList().apply {
        val startWithNumber = Regex("""^\d+$""")
        if (startWithNumber.matches(this@buildTextFindMongo)) {
            queryStartWithNumber().forEach {
                add(it equal regexQuery)
            }
        } else {
            queryStartWithString().forEach {
                add(it equal regexQuery)
            }
        }

        queryField().forEach {
            add(it equal regexQuery)
        }
    }
    val queryTextReg = "\$or" equal queryTextCondition
    val queryFixOrgIdDoc = "orgIndex" equal ObjectId(orgId)
    val fullQuery = BasicBSONList().apply {
        add(queryFixOrgIdDoc)
        add(queryTextReg)
    }
    return "\$and" equal fullQuery
}
