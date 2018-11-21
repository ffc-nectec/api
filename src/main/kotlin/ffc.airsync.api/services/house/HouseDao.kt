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

package ffc.airsync.api.services.house

import ffc.airsync.api.services.DEFAULT_MONGO_HOST
import ffc.airsync.api.services.DEFAULT_MONGO_PORT
import ffc.airsync.api.services.Dao
import ffc.airsync.api.services.Sync
import ffc.entity.place.House

interface HouseDao : Dao, Sync<House> {
    fun insert(orgId: String, house: House): House
    fun insert(orgId: String, house: List<House>): List<House>

    fun update(orgId: String, house: House): House?
    fun update(orgId: String, houseList: List<House>): List<House>

    fun delete(orgId: String, houseId: String)

    fun findAll(orgId: String, query: String? = null, haveLocation: Boolean? = true): List<House>
    fun find(orgId: String, houseId: String): House?

    fun removeByOrgId(orgId: String)
}

val houses: HouseDao by lazy { MongoHouseDao(DEFAULT_MONGO_HOST, DEFAULT_MONGO_PORT) }
