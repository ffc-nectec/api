package ffc.airsync.api.services.house

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.filter.Developer
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.util.GEOJSONHeader
import ffc.airsync.api.services.util.containsSome
import ffc.airsync.api.services.util.getLoginRole
import ffc.airsync.api.services.util.paging
import ffc.entity.Person
import ffc.entity.User
import ffc.entity.place.House
import ffc.entity.update
import me.piruin.geok.geometry.Feature
import me.piruin.geok.geometry.FeatureCollection
import javax.annotation.security.RolesAllowed
import javax.ws.rs.BadRequestException
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
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

const val NEWPART_HOUSESERVICE = "house"

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

class HouseResourceNewEndpoint {

    @Context
    private lateinit var context: SecurityContext

    @POST
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun createSingle(@PathParam("orgId") orgId: String, house: House?): Response {
        if (house == null) throw BadRequestException()

        val role = context.getLoginRole()
        return Response.status(Response.Status.CREATED).entity(houseService.create(orgId, role, house)).build()
    }

    @POST
    @Path("/$ORGIDTYPE/houses")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun create(@PathParam("orgId") orgId: String, houseList: List<House>?): Response {
        if (houseList == null) throw BadRequestException()

        val role = context.getLoginRole()
        return Response.status(Response.Status.CREATED).entity(houseService.create(orgId, role, houseList)).build()
    }

    @Developer
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 5)
    fun getGeoJsonHouse(
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int,
        @PathParam("orgId") orgId: String
    ): FeatureCollection<House> {
        val houses = houseService.getHouses(orgId, haveLocation = true)
        if (houses.isEmpty()) throw NoSuchElementException("ไม่มีรายการบ้าน")
        return FeatureCollection(houses.map { Feature(it.location!!, it) })
    }

    @Developer
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE.geojson")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 5)
    fun getGeoJsonTypeHouse(
        @QueryParam("page") @DefaultValue("1") page: Int,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int,
        @PathParam("orgId") orgId: String
    ): FeatureCollection<House> {
        return getGeoJsonHouse(page, per_page, orgId)
    }

    @Developer
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
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
        return houseService.getHouses(orgId, query, haveLocation).paging(page, per_page)
    }

    @Developer
    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE.json")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
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

    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}/resident")
    @RolesAllowed("USER", "ORG", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getPersonInHouse(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): List<Person> {
        houseService.getSingle(orgId, houseId).let {
            require(it != null) { "ไม่พบข้อมูลบ้าน" }
            require(it.no?.trim() != "0") { "ไม่สามารถดูสมาชิกในบ้านนอกเขตได้" }
        }
        return houseService.getPerson(orgId, houseId)
    }

    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getSingle(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): House {
        return houseService.getSingle(orgId, houseId) ?: throw NoSuchElementException("ไม่พบรหัสบ้าน $houseId")
    }

    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}.json")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getSingleJsonType(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): House {
        return getSingle(orgId, houseId)
    }

    @PUT
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ): Response {
        val role = context.getLoginRole()
        when {
            role.containsSome(User.Role.ORG, User.Role.ADMIN) -> house.link?.isSynced = true
            else -> house.link?.isSynced = false
        }

        val houseUpdate = houseService.update(orgId, house.update { }, houseId)
        return Response.status(200).entity(houseUpdate).build()
    }

    @PUT
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun updateFail(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ) {
        require(false) { "URL สำหรับการ update ข้อมูลผิด" }
    }

    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("USER", "ORG", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 2)
    fun getSingleGeo(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): FeatureCollection<House> {
        return houseService.getSingleGeo(orgId, houseId) ?: throw NoSuchElementException("ไม่พบรหัสบ้าน $houseId")
    }

    @GET
    @Path("/$ORGIDTYPE/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}.geojson")
    @RolesAllowed("USER", "ORG", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 2)
    fun getSingleGeoType(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): FeatureCollection<House> {
        return getSingleGeo(orgId, houseId)
    }

    @DELETE
    @Path("/$ORGIDTYPE/${NEWPART_HOUSESERVICE}s")
    @RolesAllowed("ORG", "ADMIN")
    fun delete(@PathParam("orgId") orgId: String): Response {
        houses.removeByOrgId(orgId)
        return Response.status(Response.Status.OK).build()
    }
}
