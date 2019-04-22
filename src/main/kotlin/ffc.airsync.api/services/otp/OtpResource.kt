package ffc.airsync.api.services.otp

import ffc.airsync.api.services.ORGIDTYPE
import org.joda.time.DateTime
import java.util.Date
import javax.annotation.security.RolesAllowed
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/org")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class OtpResource(
    val otpDao: OtpDao = otp
) {
    @GET
    @Path("/$ORGIDTYPE/otp")
    @RolesAllowed("ORG", "ADMIN")
    fun get(@PathParam("orgId") orgId: String): Map<String, String> {
        return mapOf("otp" to otpDao.get(orgId))
    }

    @POST
    @Path("/$ORGIDTYPE/otp")
    @RolesAllowed("USER", "PROVIDER", "SURVEYOR", "PATIENT")
    fun validate(
        @PathParam("orgId") orgId: String,
        clientOtp: Map<String, String>
    ): Map<String, Boolean> {
        val timestamp = Date(System.currentTimeMillis())
        val otpString = clientOtp.getValue("otp")
        val checkCurrentTime = otpDao.isValid(
            orgId = orgId,
            otp = otpString,
            timestamp = timestamp
        )
        val checkPastTime = otpDao.isValid(
            orgId = orgId,
            otp = otpString,
            timestamp = timestamp.minusMillis(60000)
        )
        if (checkCurrentTime || checkPastTime) return mapOf("isValid" to true)
        else throw NotAuthorizedException("Cannot auth otp.")
    }

    private fun Date.minusMillis(millis: Int): Date {
        val joda = DateTime(time)
        return Date(joda.minusMillis(millis).millis)
    }
}
