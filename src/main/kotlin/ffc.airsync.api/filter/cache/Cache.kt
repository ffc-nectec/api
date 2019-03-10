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
    val private: Boolean = false,
    val noStore: Boolean = false,
    val noCache: Boolean = false,
    val mustRevalidate: Boolean = false,
    val noTransform: Boolean = false
)

fun Cache.toCacheControl(): CacheControl {
    val age = maxAge
    return CacheControl().apply {
        if (noStore) {
            isNoStore = true
            return@apply
        }
        isPrivate = private
        isNoCache = noCache
        isMustRevalidate = mustRevalidate
        isNoTransform = noTransform
        this.maxAge = age
    }
}

fun CacheControl.combineWith(request: CacheControl): CacheControl {
    if (this.isNoStore || request.isNoStore) {
        return CacheControl().apply {
            isNoTransform = false
            isNoStore = true
        }
    }
    if (request.isNoCache) isNoCache = true
    if (request.isMustRevalidate) isMustRevalidate = true
    if (request.isNoTransform) isNoTransform = true
    if (request.isPrivate) isPrivate = true
    if (maxAge > -1) {
        maxAge = request.maxAge.takeIf { -1 < it && it < maxAge } ?: maxAge
    }
    return this
}
