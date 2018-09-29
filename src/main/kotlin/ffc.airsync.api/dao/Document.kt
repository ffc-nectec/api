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

package ffc.airsync.api.dao

import com.mongodb.client.FindIterable
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.BasicBSONList
import kotlin.collections.map as mapKt

inline fun <reified T> FindIterable<Document>.firstAs(): T = first().toJson().parseTo<T>()

inline fun <reified T> FindIterable<Document>.listOf(): List<T> = mapKt { it.toJson().parseTo<T>() }

fun bsonListOf(vararg document: Document): BasicBSONList = BasicBSONList().apply { document.forEach { add(it) } }

fun documentOf(vararg pair: Pair<String, Any?>): Document = Document(pair.toMap())

fun Any.toDocument(): Document = Document.parse(toJson())

internal infix fun String.equal(param: Any?): Document = Document(this, param)

internal infix fun Document.plus(doc: Document): Document {
    doc.forEach { key, value ->
        this.append(key, value)
    }
    return this
}
