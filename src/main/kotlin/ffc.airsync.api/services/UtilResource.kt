/*
 * Copyright (c) 2561 NECTEC
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

package ffc.airsync.api.services

import ffc.airsync.api.services.filter.Cache
import org.joda.time.DateTime
import java.sql.Timestamp
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UtilResource {
    @Cache(maxAge = 2)
    @GET
    fun getRootPart(): Response {
        return Response.status(200).entity(Runtime.getRuntime().freeMemory()).build()
    }

    @Cache(maxAge = 1)
    @GET
    @Path("/datetime")
    fun time(): DateTime {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(7))))
        return DateTime.now()
    }

    @Cache(maxAge = 1)
    @GET
    @Path("/timestamp")
    fun timestamp(): Timestamp {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(7))))
        return Timestamp(DateTime.now().millis)
    }
}
