package ffc.airsync.api.services.filter

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.ws.rs.NameBinding

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
    val noCache: Boolean = false
)
