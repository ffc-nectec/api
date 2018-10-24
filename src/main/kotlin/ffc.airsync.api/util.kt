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
import ffc.entity.Identity
import ffc.entity.Lang
import ffc.entity.Place
import ffc.entity.User
import ffc.entity.gson.HealthCareJsonAdapter
import ffc.entity.gson.IdentityJsonAdapter
import ffc.entity.gson.PlaceJsonAdapter
import ffc.entity.gson.URLsJsonAdapter
import ffc.entity.gson.UserJsonAdapter
import ffc.entity.gson.parseTo
import ffc.entity.healthcare.HealthCareService
import ffc.entity.util.URLs
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Geometry
import me.piruin.geok.gson.GeometrySerializer
import me.piruin.geok.gson.LatLngSerializer
import me.piruin.geok.gson.adapterFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.nio.charset.Charset
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone

val debug = System.getenv("FFC_DEBUG")
fun <T> printDebug(infoDebug: T) {
    if (debug == null) println(infoDebug)
}

val DATETIMEBANGKOK: DateTime get() = DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Bangkok"))))

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

val airSyncGson = GsonBuilder()
    .adapterFor<User>(UserJsonAdapter())
    .adapterFor<Identity>(IdentityJsonAdapter())
    .adapterFor<HealthCareService>(HealthCareJsonAdapter())
    .adapterFor<Place>(PlaceJsonAdapter())
    .adapterFor<URLs>(URLsJsonAdapter())
    .adapterFor<Geometry>(GeometrySerializer())
    .adapterFor<LatLng>(LatLngSerializer())
    .adapterFor<DateTime>(DateTimeConverter())
    .adapterFor<LocalDate>(LocalDateConverter())
    .adapterFor<LocalDateTime>(LocalDateTimeConverter())
    .create()
