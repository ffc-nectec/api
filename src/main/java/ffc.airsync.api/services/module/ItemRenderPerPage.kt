package ffc.airsync.api.services.module

import ffc.airsync.api.printDebug
import javax.ws.rs.NotFoundException

interface AddItmeAction {
    fun onAddItemAction(itemIndex: Int)
}

fun itemRenderPerPage(page: Int, per_page: Int, count: Int, onAddItemAction: AddItmeAction) {
    printDebug("Item per page page=$page per_page=$per_page count=$count")

    val fromItem = ((page - 1) * per_page) + 1
    var toItem = (page) * per_page

    if (fromItem > count) throw NotFoundException("ไม่พบ")
    if (toItem > count) {
        toItem = count
    }

    printDebug("page $page per_page $per_page")
    printDebug("from $fromItem to $toItem")

    try {
        (fromItem..toItem).forEach {
            onAddItemAction.onAddItemAction(it - 1)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        throw ex
    }
}
