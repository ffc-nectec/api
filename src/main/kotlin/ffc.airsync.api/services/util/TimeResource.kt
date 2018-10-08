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

package ffc.airsync.api.services.util

import ffc.airsync.api.DATETIMEBANGKOK
import ffc.airsync.api.TIMESTAMPBANGKOK
import ffc.airsync.api.filter.Cache
import org.joda.time.DateTime
import java.sql.Timestamp
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TimeResource {
    @GET
    @Cache(maxAge = 2)
    fun getRootPart(): Response {
        return Response.status(200).entity(Runtime.getRuntime().freeMemory()).build()
    }

    @GET
    @Path("/servertime")
    @Cache(maxAge = 1)
    fun time(): TimeData {
        return TimeData(DATETIMEBANGKOK)
    }

    @GET
    @Path("/timestamp")
    @Cache(maxAge = 1)
    fun timestamp(): Timestamp {
        return TIMESTAMPBANGKOK
    }
}

data class TimeData(val dateTime: DateTime)
