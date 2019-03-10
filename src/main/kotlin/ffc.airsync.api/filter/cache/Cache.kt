package ffc.airsync.api.filter.cache

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.ws.rs.NameBinding
import javax.ws.rs.core.CacheControl

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS,
    AnnotationTarget.FILE
)
annotation class Cache(
    val maxAge: Int = -1,
    val isPrivate: Boolean = false,
    val noStore: Boolean = false,
    val noCache: Boolean = false,
    val mustRevalidate: Boolean = false,
    val noTransform: Boolean = false
)

fun Cache.combineWith(request: CacheControl): CacheControl {
    val cc = CacheControl().apply {
        isNoTransform = false //default is true
    }

    if (noStore || request.isNoStore) {
        return cc.apply { isNoStore = true }
    }
    if (noCache || request.isNoCache) cc.isNoCache = true
    if (mustRevalidate || request.isMustRevalidate) cc.isMustRevalidate = true
    if (noTransform || request.isNoTransform) cc.isNoTransform = true
    if (isPrivate || request.isPrivate) cc.isPrivate = true
    if (maxAge > -1) {
        cc.maxAge = request.maxAge.takeIf { -1 < it && it < maxAge } ?: maxAge
    }
    return cc
}
