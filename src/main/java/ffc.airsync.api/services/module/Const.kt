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

package ffc.airsync.api.services.module

import ffc.airsync.api.dao.DaoFactory
import ffc.airsync.api.dao.DiseaseDao
import ffc.airsync.api.dao.HouseDao
import ffc.airsync.api.dao.OrgDao
import ffc.airsync.api.dao.PersonDao
import ffc.airsync.api.dao.TokenDao
import ffc.airsync.api.dao.UserDao

val orgDao = DaoFactory().build<OrgDao>()
val tokenDao = DaoFactory().build<TokenDao>()
val personDao = DaoFactory().build<PersonDao>()
val userDao = DaoFactory().build<UserDao>()
val houseDao = DaoFactory().build<HouseDao>()
val diseaseDao = DaoFactory().build<DiseaseDao>()
