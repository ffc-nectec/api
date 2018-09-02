package ffc.airsync.api.dao

import ffc.airsync.api.security.password
import ffc.entity.User
import ffc.entity.copy
import ffc.entity.gson.toJson
import org.bson.Document
import org.bson.types.ObjectId

fun User.toDocument(): Document {
    val user = this.copy(ObjectId().toHexString())
    val document = Document.parse(user.toJson())
    document.append("password", password().hash(user.password))
    return document
}
