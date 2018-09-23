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

internal class DaoFactory {
    companion object {
        var host = "127.0.0.1"
        var port = 27017
    }

    fun orgs(host: String = DaoFactory.host, port: Int = DaoFactory.port): OrgDao = MongoOrgDao(host, port)
    fun users(host: String = DaoFactory.host, port: Int = DaoFactory.port): UserDao = MongoUserDao(host, port)
    fun houses(host: String = DaoFactory.host, port: Int = DaoFactory.port): HouseDao = MongoHouseDao(host, port)
    fun persons(host: String = DaoFactory.host, port: Int = DaoFactory.port): PersonDao = MongoPersonDao(host, port)
    fun tokens(host: String = DaoFactory.host, port: Int = DaoFactory.port): TokenDao = MongoTokenDao(host, port)
    fun diseases(host: String = DaoFactory.host, port: Int = DaoFactory.port): DiseaseDao = MongoDiseaseDao(host, port)
    fun homeHealthTypes(host: String = DaoFactory.host, port: Int = DaoFactory.port): HomeHealthTypeDao =
        MongoHomeHealthTypeDao(host, port)

    fun healthCareServices(host: String = DaoFactory.host, port: Int = DaoFactory.port): HealthCareServiceDao =
        MongoHealthCareServiceDao(host, port)
}
