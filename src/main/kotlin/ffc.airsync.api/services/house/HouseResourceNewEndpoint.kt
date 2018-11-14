package ffc.airsync.api.services.house

import ffc.airsync.api.filter.Cache
import ffc.airsync.api.filter.Developer
import ffc.airsync.api.services.ORGIDTYPE
import ffc.airsync.api.services.util.GEOJSONHeader
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

const val NEWPART_HOUSESERVICE = "house"

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

class HouseResourceNewEndpoint {

    @Context
    private var context: SecurityContext? = null

    @Developer
    @GET
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 5)
    fun getGeoJsonHouse(
        @QueryParam("page") page: Int = 1,
        @QueryParam("per_page") per_page: Int = 200,
        @PathParam("orgId") orgId: String
    ): FeatureCollection<House> {
        val houses = HouseService.getHouses(orgId, haveLocation = true)
        if (houses.isEmpty()) throw NotFoundException("ไม่มีรายการบ้าน")
        val geoReturn = FeatureCollection<House>()
        geoReturn.features.addAll(houses.map { Feature(it.location!!, it) })
        return geoReturn
    }

    @Developer
    @GET
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 5)
    fun newGetJsonHouse(
        @QueryParam("page") @DefaultValue("1") page: Int = 1,
        @QueryParam("per_page") @DefaultValue("200") per_page: Int = 200,
        @QueryParam("query") query: String? = null,
        @QueryParam("haveLocation") haveLocationQuery: String? = null,
        @PathParam("orgId") orgId: String
    ): List<House> {
        val haveLocation: Boolean? = when (haveLocationQuery) {
            "true" -> true
            "false" -> false
            else -> null
        }
        return HouseService.getHouses(orgId, query, haveLocation).paging(page, per_page)
    }

    @PUT
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun update(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ): Response {

        when (context?.getLoginRole()) {
            User.Role.ORG -> house.update(house.timestamp) {
                house.link?.isSynced = true
            }
            User.Role.ADMIN -> house.update(house.timestamp) {
                house.link?.isSynced = true
            }
            else -> house.update {
                house.link?.isSynced = false
            }
        }

        val houseUpdate = HouseService.update(orgId, house, houseId)
        return Response.status(200).entity(houseUpdate).build()
    }

    @PUT
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun updateFail(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String,
        house: House
    ) {
        require(false) { "URL สำหรับการ update ข้อมูลผิด" }
    }

    @GET
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("USER", "ORG", "PROVIDER", "SURVEYOR")
    @Produces(GEOJSONHeader)
    @Cache(maxAge = 2)
    fun getSingleGeo(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): FeatureCollection<House> {
        return HouseService.getSingleGeo(orgId, houseId) ?: throw NotFoundException("ไม่พบรหัสบ้าน $houseId")
    }

    @GET
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}/resident")
    @RolesAllowed("USER", "ORG", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getPersonInHouse(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): List<Person> {
        return HouseService.getPerson(orgId, houseId)
    }

    @GET
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE/{houseId:([\\dabcdefABCDEF]{24})}")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER", "SURVEYOR")
    @Cache(maxAge = 2)
    fun getSingle(
        @PathParam("orgId") orgId: String,
        @PathParam("houseId") houseId: String
    ): House {
        return HouseService.getSingle(orgId, houseId) ?: throw NotFoundException("ไม่พบรหัสบ้าน $houseId")
    }

    @POST
    @Path("/$ORGIDTYPE}/${NEWPART_HOUSESERVICE}s")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun create(@PathParam("orgId") orgId: String, houseList: List<House>?): Response {
        if (houseList == null) throw BadRequestException()

        return when (context?.getLoginRole()) {
            User.Role.ORG -> {
                val houseReturn = HouseService.createByOrg(orgId, houseList)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.ADMIN -> {
                val houseReturn = HouseService.createByOrg(orgId, houseList)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.USER -> {
                val houseReturn = HouseService.createByUser(orgId, houseList)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.PROVIDER -> {
                val houseReturn = HouseService.createByUser(orgId, houseList)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
        }
    }

    @POST
    @Path("/$ORGIDTYPE}/$NEWPART_HOUSESERVICE")
    @RolesAllowed("USER", "ORG", "ADMIN", "PROVIDER")
    fun createSingle(@PathParam("orgId") orgId: String, house: House?): Response {
        if (house == null) throw BadRequestException()

        return when (context?.getLoginRole()) {
            User.Role.ORG -> {
                val houseReturn = HouseService.createByOrg(orgId, house)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.ADMIN -> {
                val houseReturn = HouseService.createByOrg(orgId, house)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.USER -> {
                val houseReturn = HouseService.createByUser(orgId, house)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            User.Role.SURVEYOR -> {
                val houseReturn = HouseService.createByUser(orgId, house)
                Response.status(Response.Status.CREATED).entity(houseReturn).build()
            }
            else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
        }
    }

    @DELETE
    @Path("/$ORGIDTYPE/${NEWPART_HOUSESERVICE}s")
    @RolesAllowed("ORG", "ADMIN")
    fun delete(@PathParam("orgId") orgId: String): Response {
        houses.removeByOrgId(orgId)
        return Response.status(Response.Status.FOUND).build()
    }
}
