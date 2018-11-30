package ffc.airsync.api.services.search

fun String.containSome(vararg keyword: CharSequence, ignoreCase: Boolean = true): Boolean {
    return keyword.firstOrNull { this.contains(it, ignoreCase) } != null
}
