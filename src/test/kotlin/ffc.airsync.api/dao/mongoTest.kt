/*
 * Copyright (c) 2561 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http:// www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ffc.airsync.api.dao
import ffc.entity.House
import org.bson.types.ObjectId
import org.junit.Test
class mongoTest {

    @Test
    fun insertData() {
        val mongoHouseDao = MongoHouseDao("127.0.0.1", 27017, "ffc", "house")
        val house = House(ObjectId().toHexString())
        // house.tambon = "เขาจันทร์ วาส"
        // house.hid = 1

        val house2 = House(ObjectId().toHexString())
        // house2.tambon = "เขาขาด"
        // house2.hid = 2

        // mongoHouseDao.insert(UUID.fromString("f247ead5-6383-5e74-2d9e-8ee1f50542be"), house)
        // mongoHouseDao.insert(UUID.fromString("f247ead5-6383-5e74-2d9e-8ee1f50542be"), house2)
    }

    @Test
    fun forTest() {
        for (i in 0..3) {
            println(i)
        }
    }

    @Test
    fun testObjId() {
        var objId = ObjectId().toHexString()
        var random4 = objId.substring(7, 8) + objId.substring(13, 14) + objId.substring(20, 24)
        println(objId)
        println(random4)

        // Thread.sleep(100)
        objId = ObjectId().toHexString()
        random4 = objId.substring(7, 8) + objId.substring(13, 14) + objId.substring(20, 24)
        println(objId)
        println(random4)
    }
}