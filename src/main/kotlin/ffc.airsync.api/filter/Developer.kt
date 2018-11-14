package ffc.airsync.api.filter

import javax.ws.rs.NameBinding

@NameBinding
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.FILE
)

annotation class Developer
