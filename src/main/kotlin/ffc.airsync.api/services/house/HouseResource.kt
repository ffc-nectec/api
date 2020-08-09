/*
 * Copyright (c) 2019 NECTEC
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
 *
 */

package ffc.airsync.api.services.house

import ffc.airsync.api.checkAllowUser
import ffc.airsync.api.filter.cache.Cache
import ffc.airsync.api.getUserLoginObject
import ffc.airsync.api.isSurveyor
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.util.GEOJSONHeader
import ffc.airsync.api.services.util.getLoginRole
import ffc.airsync.api.services.util.paging
import ffc.entity.Person
import ffc.entity.User.Role.*
import ffc.entity.place.House
import ffc.entity.update
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.annotation.security.RolesAllowed
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

const val NEWPART_HOUSESERVICE = "house"

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

class HouseResourceNewEndpoint {

    @Context
    private lateinit var context: SecurityContext

    val surveyorProcess by lazy { SurveyorProcess() }

    /**
     * สร้างข้อมูลบ้าน
     * @param house ข้อมูลบ้าน
     * @param orgId รหัส ID ของหน่วยงาน
     * @return ข้อมูลบ้านที่สร้างขึ้นใหม่ในระบบ
     */
    @POST
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("ADMIN", "PROVIDER")
    fun createSingle(@PathParam("orgId") orgId: String, house: House?): Response {
        if (house == null) throw BadRequestException()

        val role = context.getLoginRole()
        return Response.status(Response.Status.CREATED).entity(houseService.create(orgId, role, house)).build()
    }

    /**
     * สร้างข้อมูลบ้านหลายหลัง
     * @param houseList ข้อมูลบ้าน
     * @param orgId รหัส ID ของหน่วยงาน
     * @return ข้อมูลบ้านที่สร้างขึ้นใหม่ในระบบ
     */
    @POST
    @Path("/$ORGIDTYPE/houses")
    @RolesAllowed("ADMIN", "PROVIDER")
    fun create(@PathParam("orgId") orgId: String, houseList: List<House>?): Response {
        if (houseList == null) throw BadRequestException()

        val role = context.getLoginRole()
        return Response.status(Response.Status.CREATED).entity(houseService.create(orgId, role, houseList)).build()
    }

    /**
     * ดึงข้อมูลบ้านภายในหน่วยงานแบบ GeoJson จะส่งออกเฉพาะบ้านที่มีพิกัดเท่านั้น
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 5)
    fun getGeoJsonHouse(
        @PathParam("orgId") orgId: String
    ): FeatureCollection<House> {
        val houses = houseService.getHouses(orgId, haveLocation = true)
        if (houses.isEmpty()) throw NoSuchElementException("ไม่มีรายการบ้าน")
        return FeatureCollection(houses.map { Feature(it.location!!, it) })
    }

    /**
     * ดึงข้อมูลบ้านภายในหน่วยงานแบบ GeoJson จะส่งออกเฉพาะบ้านที่มีพิกัดเท่านั้น
     * ผลลัพท์เหมือนกับ #getGeoJsonHouse
     * @see getGeoJsonHouse
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE.geojson")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 5)
    fun getGeoJsonTypeHouse(
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int,
        @PathParam("orgId") orgId: String
    ): FeatureCollection<House> {
        return getGeoJsonHouse(orgId)
    }

    /**
     * ดึงข้อมูลรายการบ้านแบบ Json
     * @param haveLocationQuery ถ้าเป็น true:บ้านเฉพาะมีพิกัด false:บ้านเฉพาะที่ไม่มีพิกัด null:บ้านทุกหลัง
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 5)
    fun getJsonHouse(
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int,
        @QueryParam("query") query: String?,
        @QueryParam("haveLocation") haveLocationQuery: String?,
        @PathParam("orgId") orgId: String
    ): List<House> {
        val haveLocation: Boolean? = when (haveLocationQuery) {
            "true" -> true
            "false" -> false
            else -> null
        }
        val houseQuery = houseService.getHouses(orgId, query, haveLocation)
        return houseQuery.paging(page, per_page)
    }

    /**
     * ดึงข้อมูลรายการบ้านแบบ Json ข้อมูลเหมือนกับ #getJsonHouse
     * @see getJsonHouse
     * @param haveLocationQuery ถ้าเป็น true:บ้านเฉพาะมีพิกัด false:บ้านเฉพาะที่ไม่มีพิกัด null:บ้านทุกหลัง
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE.json")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 5)
    fun getJsonTypeHouse(
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int,
        @QueryParam("query") query: String?,
        @QueryParam("haveLocation") haveLocationQuery: String?,
        @PathParam("orgId") orgId: String
    ): List<House> {
        return getJsonHouse(page, per_page, query, haveLocationQuery, orgId)
    }

    /**
     * ดึงข้อมูลคนในบ้าน ถ้าเป็น #SURVEYOR จะดูได้เฉพาะ บ้านที่ตนดูแลเท่านั้น
     * @param houseId หมายเลข Object id ของบ้าน
     * @return รายการข้อมูลคนในบ้าน
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}/resident")
    @RolesAllowed("PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getPersonInHouse(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): List<Person> {
        val userLogin = context.getUserLoginObject()
        houseService.getSingle(orgId, houseId).let {
            require(it != null) { "ไม่พบข้อมูลบ้าน" }
            require(it.no?.trim() != "0") { "ไม่สามารถดูสมาชิกในบ้านนอกเขตได้" }
            if (userLogin.isSurveyor()) {
                require(
                    (it.checkAllowUser(userLogin.id))
                ) { "User ระดับสำรวจ สามารถดูข้อมูลคนของบ้านที่ตัวเองสำรวจเท่านั้น" }
            }
        }
        return houseService.getPerson(orgId, houseId)
    }

    /**
     * ดูข้อมูลเฉพาะบ้านหลังที่ระบุ
     * @param houseId รหัส Object id ของบ้าน
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getSingle(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): House {
        return houseService.getSingle(orgId, houseId) ?: throw NoSuchElementException("ไม่พบรหัสบ้าน $houseId")
    }

    /**
     * ดูข้อมูลเฉพาะบ้านหลังที่ระบุ เหมือนกับ #getSingle
     * @param houseId รหัส Object id ของบ้าน
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}.json")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getSingleJsonType(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): House {
        return getSingle(orgId, houseId)
    }

    /**
     * แก้ไขข้อมูลบ้าน ในกรณีที่เป็น #SURVEYOR จะแก้ไขได้เฉพาะพิกัด
     * @param houseId รหัส Object id ข้องบ้าน
     * @param house Object ของบ้าน
     */
    @PUT
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ): Response {
        val userLogin = context.getUserLoginObject()
        val role = userLogin.roles
        when {
            role.contains(ADMIN) -> house.link?.isSynced = true
            else -> house.link?.isSynced = false
        }

        return when {
            /**
             * SURVEYOR สามารถปักพิกัดได้เฉพาะหลังที่ไม่มีพิกัดเท่านั้น
             */
            role.contains(SYNC_AGENT) -> {
                Response.status(200).entity(houseService.update(orgId, house, houseId)).build()
            }
            role.contains(SURVEYOR) -> {
                val original = houseService.getSingle(orgId, houseId)!!
                val build = surveyorProcess.process(original, house, userLogin.id)
                Response.status(200).entity(houseService.update(orgId, build, houseId)).build()
            }
            else -> {
                Response.status(200).entity(houseService.update(orgId, house.update { }, houseId)).build()
            }
        }
    }

    @PUT
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("ADMIN", "PROVIDER", "SURVEYOR")
    fun updateFail(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ) {
        require(false) { "URL สำหรับการ update ข้อมูลผิด" }
    }

    /**
     * ดึงข้อมูล GeoJson สำหรับวาดของบ้าน 1 หลัง
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 2)
    fun getSingleGeo(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): FeatureCollection<House> {
        return houseService.getSingleGeo(orgId, houseId) ?: throw NoSuchElementException("ไม่พบรหัสบ้าน $houseId")
    }

    /**
     * ดึงข้อมูล GeoJson สำหรับวาดของบ้าน 1 หลัง
     * เหมือนกับ #getSingleGeo
     */
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}.geojson")
    @RolesAllowed("PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 2)
    fun getSingleGeoType(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): FeatureCollection<House> {
        return getSingleGeo(orgId, houseId)
    }

    /**
     * ลบข้อมูลบ้านในหน่วยงานนั้น ทั้งหมด
     * ใช้สำหรับตอน clear ข้อมูลก่อนรับข้อมูลจากระบบ HIS
     */
    @DELETE
    @Path("/$ORGIDTYPE/${NEWPART_HOUSESERVICE}s")
    @RolesAllowed("ADMIN")
    fun delete(@PathParam("orgId") orgId: String): Response {
        houses.removeByOrgId(orgId)
        return Response.status(Response.Status.OK).build()
    }
}
