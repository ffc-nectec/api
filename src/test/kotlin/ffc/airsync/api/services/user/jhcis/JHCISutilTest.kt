/*
 * Copyright (c) 2019 NECTEC
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
 *
 */

package ffc.airsync.api.services.user.jhcis

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class JHCISutilTest {
    private val jhciSutil = JHCISutil()
    @Test
    fun md5pass() {
        jhciSutil.md5pass("thanachai") `should be equal to` "dcc474bf6c5324d3bbdca5f309ad0182"
        jhciSutil.md5pass("jhcisxxx") `should be equal to` "f62c83eb0036c8b2c1744fd434651868"
    }
}
