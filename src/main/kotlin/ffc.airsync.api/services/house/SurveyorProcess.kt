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

package ffc.airsync.api.services.house

import ffc.airsync.api.checkAllowUser
import ffc.entity.copy
import ffc.entity.place.House
import ffc.entity.update

/**
 * จัดการเกี่ยวกับ Surveyor
 * ช่วยกรองว่าสามารถแก้ไขค่าอะไรในบ้านได้บ้าง
 * โดยจะนำข้อมูลที่ส่งมา แล้วคัดแต่อันที่แก้ไขได้ มาใส่อันเดิม
 */
class SurveyorProcess {
    fun process(original: House, surveyorHouse: House, userId: String = "xxxx"): House {
        val originalCopy = original.copy()
        if (originalCopy.noPosition() || originalCopy.checkAllowUser(userId))
            surveyorHouse.location?.let {
                originalCopy.location = it.copy()
            }
        else throw IllegalStateException("นักสำรวจสามารถปักพิกัดได้เฉพาะ บ้านที่ไม่มีพิกัด และ บ้านที่ตัวเองรับผิดชอบ")
        return originalCopy.update(surveyorHouse.timestamp) {}
    }

    private fun House.noPosition(): Boolean {
        val location = this.location ?: return true
        if (location.coordinates.latitude == 0.0 || location.coordinates.longitude == 0.0) return true
        return false
    }
}
