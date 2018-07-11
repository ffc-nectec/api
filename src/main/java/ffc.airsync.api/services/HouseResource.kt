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

import ffc.airsync.api.printDebug
import ffc.airsync.api.services.module.HouseService
import ffc.entity.House
import ffc.entity.User
import ffc.entity.gson.toJson
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
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

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/org")
class HouseResource {

    @Context
    private var context: SecurityContext? = null

    @RolesAllowed("USER", "ORG")
    @Produces(GEOJSONHeader)
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/house")
    fun getGeoJsonHouse(@QueryParam("page") page: Int = 1, @QueryParam("per_page") per_page: Int = 200, @QueryParam("hid") hid: Int = -1, @QueryParam("haveLocation") haveLocation: Boolean? = null, @PathParam("orgId") orgId: String, @Context req: HttpServletRequest): FeatureCollection<House> {

        printDebug("getGeoJsonHouse house method geoJson List")

        printDebug("\tParamete orgId $orgId page $page per_page $per_page hid $hid haveLocation $haveLocation")

        val geoJso = HouseService.getGeoJsonHouse(orgId, if (page == 0) 1 else page, if (per_page == 0) 200 else per_page, haveLocation, req.queryString
                ?: "")

        val geoReturn = FeatureCollection<House>()

        if (geoJso.features.isNotEmpty()) {
            geoJso.features.forEach {
                val house = it.properties
                if (house!!.location != null) {
                    geoReturn.features.add(Feature(it.geometry, it.properties))
                }
            }
        }

        printDebug("Print feture before return to rest")
        if (geoReturn.features.isEmpty()) throw NotFoundException("ไม่มีรายการบ้าน")
        return geoReturn
    }

    @RolesAllowed("USER", "ORG")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/house")
    fun getJsonHouse(@Context req: HttpServletRequest, @QueryParam("page") page: Int = 1, @QueryParam("per_page") per_page: Int = 200, @QueryParam("hid") hid: Int = -1, @QueryParam("haveLocation") haveLocation: Boolean? = null, @PathParam("orgId") orgId: String): List<House> {
        printDebug("getGeoJsonHouse house method geoJson List paramete orgId $orgId page $page per_page $per_page hid $hid")
        return HouseService.getJsonHouse(
                orgId,
                if (page == 0) 1 else page,
                if (per_page == 0) 200 else per_page,
                haveLocation,
                req.queryString
                        ?: "")
    }

    @RolesAllowed("USER", "ORG")
    @PUT
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/house/{houseId:([\\dabcdefABCDEF]{24})}")
    fun update(@PathParam("orgId") orgId: String, @PathParam("houseId") houseId: String, house: House): Response {
        printDebug("Call put house by ip OrgID $orgId")

        printDebug("\tid ${house.id} latLng ${house.location}")

        if (context == null) {
            printDebug("\tContext is null")
        }
        val role = getTokenRole(context!!)
        printDebug("\tRole $role")

        printDebug("\t${context!!.userPrincipal}")

        // if (house.location == null) throw javax.ws.rs.NotSupportedException("coordinates null")
        val houseUpdate = HouseService.update(role, orgId, house, houseId)

        return Response.status(200).entity(houseUpdate).build()
    }

    @RolesAllowed("USER", "ORG")
    @Produces(GEOJSONHeader)
    @Consumes(GEOJSONHeader)
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/house/{houseId:([\\dabcdefABCDEF]{24})}")
    fun getSingleGeo(@PathParam("orgId") orgId: String, @PathParam("houseId") houseId: String): FeatureCollection<House> {
        printDebug("Call getGeoJsonHouse single geo json OrgID $orgId House ID = $houseId")
        val house: FeatureCollection<House> = HouseService.getSingleGeo(orgId, houseId)

        return house
    }

    @RolesAllowed("USER", "ORG")
    @GET
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/house/{houseId:([\\dabcdefABCDEF]{24})}")
    fun getSingle(@PathParam("orgId") orgId: String, @PathParam("houseId") houseId: String): House {
        printDebug("Call getGeoJsonHouse single house OrgID $orgId House ID = $houseId")
        val house: House = HouseService.getSingle(orgId, houseId)

        return house
    }

    @RolesAllowed("ORG", "USER")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/houses")
    fun create(@PathParam("orgId") orgId: String, houseList: List<House>?): Response {
        printDebug("\nCall create house")
        if (houseList == null) throw BadRequestException()

        if (context == null) {
            printDebug("\tContext is null")
        }
        val role = getTokenRole(context!!)
        printDebug("\tRole $role")

        printDebug("\t${context!!.userPrincipal}")

        houseList.forEach {
            it.people = null
            // it.haveChronics = null
            // it.haveChronic=null
            printDebug("house json = " + it.toJson())
        }

        if (role == User.Role.ORG) {
            val houseReturn = HouseService.createByOrg(orgId, houseList)
            return Response.status(Response.Status.CREATED).entity(houseReturn).build()
        } else if (role == User.Role.USER) {

            val houseReturn = HouseService.createByUser(orgId, houseList)
            return Response.status(Response.Status.CREATED).entity(houseReturn).build()
        }
        throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
    }

    @RolesAllowed("ORG", "USER")
    @POST
    @Path("/{orgId:([\\dabcdefABCDEF].*)}/place/house")
    fun createSingle(@PathParam("orgId") orgId: String, house: House?): Response {

        printDebug("\nCall create house")
        if (house == null) throw BadRequestException()
        if (context == null) {
            printDebug("\tContext is null")
        }
        val role = getTokenRole(context!!)
        printDebug("\tRole $role")

        printDebug("\t${context!!.userPrincipal}")
        house.people = null
        // house.haveChronics = null
        printDebug("house json = " + house.toJson())

        if (role == User.Role.ORG) {
            val houseReturn = HouseService.createByOrg(orgId, house)
            return Response.status(Response.Status.CREATED).entity(houseReturn).build()
        } else if (role == User.Role.USER) {
            val houseReturn = HouseService.createByUser(orgId, house)
            return Response.status(Response.Status.CREATED).entity(houseReturn).build()
        }
        throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
    }
}
