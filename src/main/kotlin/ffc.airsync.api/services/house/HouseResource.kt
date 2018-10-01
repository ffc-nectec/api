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

package ffc.airsync.api.services.house

import ffc.airsync.api.services.filter.Cache
import ffc.airsync.api.services.util.GEOJSONHeader
import ffc.airsync.api.services.util.getTokenRole
import ffc.entity.House
import ffc.entity.User
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.annotation.security.RolesAllowed
import javax.ws.rs.BadRequestException
import javax.ws.rs.Consumes
import javax.ws.rs.ForbiddenException
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

const val PART_HOUSESERVICE = "place/house"

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/org")
class HouseResource {
    @Context
    private var context: SecurityContext? = null

    @Cache(maxAge = 5)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HOUSESERVICE")
    fun getGeoJsonHouse(
        @QueryParam("page") page: Int = 1,
        @QueryParam("per_page") per_page: Int = 200,
        @PathParam("orgId") orgId: String
    ): FeatureCollection<House> {
        val houses = HouseService.getHouses(orgId,
            if (page == 0) 1 else page,
            if (per_page == 0) 200 else per_page,
            haveLocation = true)
        if (houses.isEmpty()) throw NotFoundException("ไม่มีรายการบ้าน")
        val geoReturn = FeatureCollection<House>()
        geoReturn.features.addAll(houses.map { Feature(it.location!!, it) })
        return geoReturn
    }

    @Cache(maxAge = 5)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HOUSESERVICE")
    fun getJsonHouse(
        @QueryParam("page") page: Int = 1,
        @QueryParam("per_page") per_page: Int = 200,
        @QueryParam("haveLocation") haveLocationQuery: String? = null,
        @PathParam("orgId") orgId: String
    ): List<House> {
        val haveLocation: Boolean? = when (haveLocationQuery) {
            "true" -> true
            "false" -> false
            else -> null
        }
        return HouseService.getHouses(
            orgId,
            if (page == 0) 1 else page,
            if (per_page == 0) 200 else per_page,
            haveLocation)
    }

    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    @PUT
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ): Response {
        val role = getTokenRole(context!!)
        val houseUpdate = HouseService.update(role, orgId, house, houseId)
        return Response.status(200).entity(houseUpdate).build()
    }

    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    @PUT
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HOUSESERVICE")
    fun updateFail(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ) {
        require(false) { "URL สำหรับการ update ข้อมูลผิด" }
    }

    @Cache(maxAge = 2)
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    fun getSingle(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): House {
        return HouseService.getSingle(orgId, houseId) ?: throw NotFoundException("ไม่พบรหัสบ้าน $houseId")
    }

    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/${PART_HOUSESERVICE}s")
    fun create(
        @PathParam("orgId") orgId: String,
        houseList: List<House>?
    ): Response {
        if (houseList == null) throw BadRequestException()
        val role = getTokenRole(context!!)
        // houseList.forEach { it.people = null }
        return when (role) {
            User.Role.ORG -> {
                val houseReturn = HouseService.createByOrg(orgId, houseList)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.USER -> {
                val houseReturn = HouseService.createByUser(orgId, houseList)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
        }
    }

    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/$PART_HOUSESERVICE")
    fun createSingle(@PathParam("orgId") orgId: String, house: House?): Response {
        if (house == null) throw BadRequestException()
        // house.people = null
        val role = getTokenRole(context!!)
        return when (role) {
            User.Role.ORG -> {
                val houseReturn = HouseService.createByOrg(orgId, house)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.USER -> {
                val houseReturn = HouseService.createByUser(orgId, house)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
        }
    }
}
