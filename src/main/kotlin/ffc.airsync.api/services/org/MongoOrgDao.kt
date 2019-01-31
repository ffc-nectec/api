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

package ffc.airsync.api.services.org

import com.mongodb.client.FindIterable
import ffc.airsync.api.printDebug
import ffc.airsync.api.services.MongoDao
import ffc.airsync.api.services.util.equal
import ffc.airsync.api.services.util.toDocument
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import java.net.InetSocketAddress

class MongoOrgDao(host: String, port: Int) : OrgDao, MongoDao(host, port, "ffc", "organ") {

    constructor(address: InetSocketAddress) : this(address.hostName, address.port)

    override fun insert(organization: Organization): Organization {
        validate(organization)
        checkDuplication(organization)
        val userListDoc = arrayListOf<Document>()
        organization.users.forEach {
            require(it.name.isNotEmpty()) { "พบค่าว่างในตัวแปร user.name" }
            require(it.password.isNotEmpty()) { "พบค่าว่างในตัวแปร user.password" }
            require(it.isTempId) { "ข้อมูลที่จะสร้างใหม่จำเป็นต้องใช้ TempId" }

            userListDoc.add(it.toDocument())
        }
        val genId = ObjectId()
        val orgDoc = Document.parse(organization.toJson())
        orgDoc.append("users", userListDoc)
        orgDoc.append("_id", genId)
        orgDoc["id"] = genId.toHexString()
        orgDoc.append("lastKnownIp", organization.bundle["lastKnownIp"])
        orgDoc.append("token", ObjectId())
        dbCollection.insertOne(orgDoc)
        val newOrgDoc = dbCollection.find("_id" equal genId).first()

        return newOrgDoc.toJson().parseTo()
    }

    private fun checkDuplication(organization: Organization) {
        val name = dbCollection.find("name" equal organization.name).first()
        require(name == null) { "ลงทะเบียน Org ซ้ำ" }
    }

    private fun validate(organization: Organization) {
        with(organization) {
            require(organization.isAcceptName()) { "name ห้ามมีอักขระพิเศษ ${organization.name}" }
            require(isTempId) { "ไม่สามารถ Register ได้ โปรดตรวจสอบ id" }
            require(name.isNotEmpty()) { "โปรระบุชื่อ หน่วยงานที่ต้องการลงทะเบียนลงในตัวแปร name" }
            require(users.isNotEmpty()) { "โปรดลงทะเบียน user ในตัวแปร user ในหน่วยงานที่ต้องการลงทะเบียน" }
            require(users.find {
                it.roles.contains(User.Role.ADMIN) || it.roles.contains(User.Role.ORG)
            } != null) { "ไม่มี User ที่เป็น Role ADMIN" }
            link?.keys?.get("pcucode")?.let { require(it is String) { "pcucode ใน bunndel ต้องเป็น String" } }
        }
    }

    override fun remove(orgId: String) {
        printDebug("Call OrgMongoDao removeByOrgId $orgId")
        dbCollection.findOneAndDelete("id" equal orgId) ?: throw NoSuchElementException("ไม่พบ Org $orgId ที่ต้องการลบ")
    }

    override fun findAll(): List<Organization> {
        printDebug("Mongo findAll() org")
        return dbCollection.find().convertToList()
    }

    private inline fun <reified T> FindIterable<Document>.convertToList(): List<T> {
        val result = arrayListOf<T>()
        forEach {
            result.add(it.toJson().parseTo())
        }
        return result
    }

    override fun findById(orgId: String): Organization {
        printDebug("Find by orgId = $orgId")
        val query = "id" equal orgId
        val orgDocument = dbCollection.find(query).first()
        printDebug("\torgDoc ${orgDocument.toJson()}")
        return orgDocument.toJson().parseTo()
    }

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        printDebug("Mongo findAll org ip $ipAddress")
        val orgDoc = dbCollection.find("lastKnownIp" equal ipAddress)
        printDebug("\tQuery org from mongo $orgDoc")
        val orgList = orgDoc.convertToList<Organization>()
        if (orgList.isEmpty()) throw NoSuchElementException("ไม่พบรายการลงทะเบียนในกลุ่มของ Org ip $ipAddress")
        return orgList
    }

    override fun find(query: String): List<Organization> {
        return findMongo(query)
    }

    private fun findMongo(query: String): List<Organization> {
        val queryDisplayNamed = query.replace(Regex("(\\d\$)"), " $1").replace(Regex("(^รพ\\.สต\\.)"), "$1 ")
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val regexQueryShortName = Document("\$regex", query.replace(Regex("[ _.@]"), "")).append("\$options", "i")
        val queryTextCondition = BasicBSONList().apply {
            if (!Regex("^\\d.*").matches(query)) {
                add("name" equal regexQueryShortName)
            }
            val regexQueryDisplayName = Document("\$regex", "(($query)|($queryDisplayNamed))").append("\$options", "i")
            add("displayName" equal regexQueryDisplayName)

            add("tel" equal regexQuery)
            add("address" equal regexQuery)
            add("link.keys.pcucode" equal regexQuery)
        }
        val queryTextReg = "\$or" equal queryTextCondition
        val resultQuery = dbCollection.find(queryTextReg).limit(20)

        return resultQuery.map {
            it.remove("_id")
            it.toJson().parseTo<Organization>()
        }.toList()
    }
}
