package ffc.airsync.api.services.house

import ffc.airsync.api.services.util.containsSome
import ffc.entity.User
import ffc.entity.place.House
import javax.ws.rs.ForbiddenException

//TODO ย้ายเข้าใน HouseService จะกันการเรียกใช้ createByOrg หรือ createByUser โดยไม่ตรวจสอบ role ได้ดีกว่า

internal fun validateHouse(
    role: List<User.Role>,
    orgId: String,
    houseList: List<House>,
    block: Int = -1
): List<House> {
    return when {
        role.containsSome(User.Role.ORG, User.Role.ADMIN) ->
            houseService.createByOrg(orgId, houseList, block)
        role.containsSome(User.Role.USER, User.Role.SURVEYOR) ->
            houseService.createByUser(orgId, houseList, block)
        else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
    }
}

internal fun validateHouse(
    role: List<User.Role>,
    orgId: String,
    house: House
): House {
    return when {
        role.containsSome(User.Role.ORG, User.Role.ADMIN) ->
            houseService.createByOrg(orgId, house)
        role.containsSome(User.Role.USER, User.Role.SURVEYOR) ->
            houseService.createByUser(orgId, house)
        else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
    }
}
