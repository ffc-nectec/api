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

package ffc.airsync.api.dao

class DaoFactory {

    @Suppress("IMPLICIT_CAST_TO_ANY")
    inline fun <reified T : Dao> build(host: String = "127.0.0.1", port: Int = 27017): T {

        return when (T::class) {
            OrgDao::class -> MongoOrgDao(host, port)
            UserDao::class -> MongoUserDao(host, port)
            HouseDao::class -> MongoHouseDao(host, port)
            PersonDao::class -> MongoPersonDao(host, port)
            TokenDao::class -> MongoTokenDao(host, port)
            DiseaseDao::class -> MongoDiseaseDao(host, port)

            else -> throw IllegalArgumentException("ไม่สามารถสร้าง dao นี้ได้")
        } as T
    }
}
