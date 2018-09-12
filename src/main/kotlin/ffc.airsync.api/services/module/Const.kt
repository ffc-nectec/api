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

import ffc.airsync.api.dao.diseases
import ffc.airsync.api.dao.homeHealthTypes
import ffc.airsync.api.dao.houses
import ffc.airsync.api.dao.orgs
import ffc.airsync.api.dao.persons
import ffc.airsync.api.dao.tokens
import ffc.airsync.api.dao.users

val orgDao = orgs()
val tokenDao = tokens()
val personDao = persons()
val userDao = users()
val houseDao = houses()
val diseaseDao = diseases()
val homeHealtyTypeDao = homeHealthTypes()
val healthCareServices = ffc.airsync.api.dao.healthCareServices()
