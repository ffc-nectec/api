package ffc.airsync.api.services.util

fun <T> Collection<T>.containsSome(vararg elements: T): Boolean {
    return elements.firstOrNull { this.contains(it) } != null
}
