package ffc.airsync.api.services.house

import ffc.airsync.api.services.util.inRole
import ffc.entity.User
import ffc.entity.place.House
import javax.ws.rs.ForbiddenException

internal fun validateHouse(
    role: List<User.Role>,
    orgId: String,
    houseList: List<House>,
    block: Int = -1
): List<House> {
    return when {
        User.Role.ORG inRole role -> HouseService.createByOrg(orgId, houseList, block)
        User.Role.ADMIN inRole role -> HouseService.createByOrg(orgId, houseList, block)
        User.Role.USER inRole role -> HouseService.createByUser(orgId, houseList, block)
        User.Role.PROVIDER inRole role -> HouseService.createByUser(orgId, houseList, block)
        else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
    }
}

internal fun validateHouse(
    role: List<User.Role>,
    orgId: String,
    house: House
): House {
    return when {
        User.Role.ORG inRole role -> HouseService.createByOrg(orgId, house)
        User.Role.ADMIN inRole role -> HouseService.createByOrg(orgId, house)
        User.Role.USER inRole role -> HouseService.createByUser(orgId, house)
        User.Role.SURVEYOR inRole role -> HouseService.createByUser(orgId, house)
        else -> throw ForbiddenException("ไม่มีสิทธ์ ในการสร้างบ้าน")
    }
}
