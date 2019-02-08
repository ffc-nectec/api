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

import ffc.entity.Lang
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.nio.charset.Charset
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone

private val bangkokTimeZone = TimeZone.getTimeZone(ZoneId.of("Asia/Bangkok"))

val DATETIMEBANGKOK: DateTime get() = DateTime(DateTimeZone.forTimeZone(bangkokTimeZone))

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

fun Any.toDNA(): String {
    val doc = Document.parse(this.toJson())
    val type = this::class.toString()
    doc.remove("id")
    doc.remove("link")
    doc.remove("timestamp")
    return "${type.hashCode()}:${doc.toJson().hashCode()}"
}

fun <T> resorceCall(call: () -> T): T {
    try {
        return call()
    } catch (ex: Exception) {
        throw ex
    }
}
