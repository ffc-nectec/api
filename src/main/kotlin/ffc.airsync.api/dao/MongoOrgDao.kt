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

import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import ffc.airsync.api.printDebug
import ffc.entity.Organization
import ffc.entity.User
import ffc.entity.gson.parseTo
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.BasicBSONList
import org.bson.types.ObjectId
import javax.ws.rs.NotFoundException

internal class MongoOrgDao(host: String, port: Int) : OrgDao, MongoAbsConnect(host, port, "ffc", "organ") {
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
        if (name != null) {
            throw IllegalArgumentException("ลงทะเบียน Org ซ้ำ")
        }
    }

    private fun validate(organization: Organization) {
        with(organization) {
            require(isTempId) { "ไม่สามารถ Register ได้ โปรดตรวจสอบ id" }
            require(name.isNotEmpty()) { "โปรระบุชื่อ หน่วยงานที่ต้องการลงทะเบียนลงในตัวแปร name" }
            require(users.isNotEmpty()) { "โปรดลงทะเบียน user ในตัวแปร user ในหน่วยงานที่ต้องการลงทะเบียน" }
            require(users.find { it.role == User.Role.ORG } != null) { "ไม่มี User ที่เป็น Role ORG" }
        }
    }

    override fun remove(orgId: String) {
        printDebug("Call OrgMongoDao remove $orgId")
        val query = Document("id", orgId)
        dbCollection.findOneAndDelete(query) ?: throw NotFoundException("ไม่พบ Org $orgId ที่ต้องการลบ")
    }

    override fun findAll(): List<Organization> {
        printDebug("Mongo findAll() org")
        val orgCursorList = dbCollection.find()
        val orgList = docListToObj(orgCursorList)
        return orgList
    }

    private fun docListToObj(list: FindIterable<Document>): List<Organization> {
        val orgList = arrayListOf<Organization>()
        printDebug("\t\tLoad doc list.")
        list.forEach {
            printDebug("\t\t$it")
            val organization: Organization = it.toJson().parseTo()
            orgList.add(organization)
        }
        printDebug("\tReturn docListToObj")
        return orgList
    }

    override fun findById(orgId: String): Organization {
        printDebug("Find by orgId = $orgId")
        val query = Document("id", orgId)
        val orgDocument = dbCollection.find(query).first()
        printDebug("\torgDoc ${orgDocument.toJson()}")
        return orgDocument.toJson().parseTo()
    }

    override fun findByIpAddress(ipAddress: String): List<Organization> {
        printDebug("Mongo findAll org ip $ipAddress")
        val query = Document("lastKnownIp", ipAddress)
        printDebug("\tCreate query object $query")
        val orgDoc = dbCollection.find(query)
        printDebug("\tQuery org from mongo $orgDoc")
        val orgList = docListToObj(orgDoc)
        if (orgList.isEmpty()) throw NotFoundException("ไม่พบรายการลงทะเบียนในกลุ่มของ Org ip $ipAddress")
        return orgList
    }

    override fun createFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = Document("id", orgId)
        if (isOrg) {
            val firebaseTokenDoc = Document("firebaseToken", firebaseToken)
            dbCollection.updateOne(query, BasicDBObject("\$set", firebaseTokenDoc))
        } else {
            dbCollection.updateOne(query, BasicDBObject("\$push", BasicDBObject("mobileFirebaseToken", firebaseToken)))
        }
    }

    override fun removeFirebase(orgId: String, firebaseToken: String, isOrg: Boolean) {
        val query = Document("id", orgId)

        if (isOrg) {
            val removeOrgFirebaseToken = Document("firebaseToken", null)
            val removeOrgFirebaseTokenQuery = Document("\$set", removeOrgFirebaseToken)
            dbCollection.updateOne(query, removeOrgFirebaseTokenQuery)
        } else {
            val removeMobileFirebaseToken = Document("mobileFirebaseToken", firebaseToken)
            val removeMobileFirebaseTokenQuery = Document("\$pull", removeMobileFirebaseToken)
            dbCollection.updateOne(query, removeMobileFirebaseTokenQuery)
        }
    }

    override fun getFirebaseToken(orgId: String): List<String> {
        try {
            printDebug("Get firebase token.")
            val firebaseTokenList = arrayListOf<String>()
            val query = Document("id", orgId)
            val firebaseOrgDoc = dbCollection.find(query).first()
            val firebaseMobile = firebaseOrgDoc["mobileFirebaseToken"] as List<*>?
            val orgFirebase = firebaseOrgDoc["firebaseToken"].toString()
            if (orgFirebase != "null")
                firebaseTokenList.add(orgFirebase)

            firebaseMobile?.forEach {
                if (it != "null") {
                    printDebug("\t\t\t\t\t$it")
                    firebaseTokenList.add(it.toString())
                }
            }
            return firebaseTokenList
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    override fun find(query: String): List<Organization> {
        return findMongo(query)
    }

    private fun findMongo(query: String): List<Organization> {
        val result = arrayListOf<Organization>()
        val regexQuery = Document("\$regex", query).append("\$options", "i")
        val queryTextCondition = BasicBSONList().apply {
            add(Document("name", regexQuery))
            add(Document("tel", regexQuery))
            add(Document("address", regexQuery))
        }
        val queryTextReg = Document("\$or", queryTextCondition)
        val resultQuery = dbCollection.find(queryTextReg)

        resultQuery.forEach {
            it.remove("_id")
            val orgMap = it.toJson().parseTo<Organization>()
            result.add(orgMap)
        }

        return result
    }
}
