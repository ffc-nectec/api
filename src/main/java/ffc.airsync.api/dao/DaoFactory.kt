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

package ffc.airsync.api.dao


class DaoFactory(val dev: Boolean = true) {


    fun buildPcuDao(): OrgDao = MongoOrgDao("127.0.0.1", 27017, "ffc", "organ")
    //fun buildPcuDao(): OrgDao = InMemoryOrgDao.instance

    fun buildOrgUserDao(): UserDao = MongoUserDao("127.0.0.1", 27017, "ffc", "user")
    //fun buildOrgUserDao(): UserDao = InMemoryUserDao.INSTANT

    fun buildHouseDao(): HouseDao = MongoHouseDao("127.0.0.1", 27017, "ffc", "house")

    fun buildPersonDao(): PersonDao = MongoPersonDao("127.0.0.1", 27017, "ffc", "person")

    //fun buildPersonDao(): PersonDao = InMemoryPersonDao.instant

    fun buildTokenMobileMapDao(): TokenDao = MongoTokenDao("127.0.0.1", 27017, "ffc", "token")


}
