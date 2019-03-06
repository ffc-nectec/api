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
import ffc.airsync.api.filter.Cache
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

private val MB = 1024L * 1024L

@Path("/system")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SystemResource {

    @GET
    @Path("/memory")
    @Cache(maxAge = 3)
    fun getFreeMemory(): Map<String, Number> {
        val rt = Runtime.getRuntime()
        return mapOf(
            "usage" to (rt.totalMemory() - rt.freeMemory()) / MB,
            "free" to rt.freeMemory() / MB,
            "total" to rt.totalMemory() / MB,
            "max" to rt.maxMemory() / MB
        )
    }

    @GET
    @Path("/time")
    @Cache(maxAge = 1)
    fun time(): Map<String, Any> {
        return mapOf("datetime" to DATETIMEBANGKOK)
    }
}
