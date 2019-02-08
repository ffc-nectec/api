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

package ffc.airsync.api.services.util

import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger("ffc.airsync.api.services.util")

interface AddItmeAction {
    fun onAddItemAction(itemIndex: Int)
}

private fun itemRenderPerPage(page: Int, per_page: Int, count: Int, onAddItemAction: AddItmeAction) {
    logger.debug("Item per page page=$page per_page=$per_page count=$count")
    val fromItem = ((page - 1) * per_page) + 1
    var toItem = (page) * per_page

    if (fromItem > count) throw NullPointerException("ไม่พบ")
    if (toItem > count) {
        toItem = count
    }

    logger.debug("page $page per_page $per_page from $fromItem to $toItem")

    try {
        (fromItem..toItem).forEach {
            onAddItemAction.onAddItemAction(it - 1)
        }
    } catch (ex: Exception) {
        logger.error(ex.message)
        throw ex
    }
}

fun <T> List<T>.paging(page: Int, per_page: Int): MutableList<T> {
    val pageList: MutableList<T> = mutableListOf()
    itemRenderPerPage(page, per_page, this.count(), object : AddItmeAction {
        override fun onAddItemAction(itemIndex: Int) {
            pageList.add(this@paging[itemIndex])
        }
    })
    return pageList
}
