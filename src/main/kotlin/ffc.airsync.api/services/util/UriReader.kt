package ffc.airsync.api.services.util

import java.io.File
import java.net.URI

class UriReader(val uri: URI) {

    fun readAsString(): String {
        val inputStream = when (uri.scheme) {
            "http", "https" -> uri.toURL().openStream()
            "file" -> File(uri).inputStream()
            else -> throw IllegalArgumentException("Not support [${uri.scheme}] scheme")
        }
        return inputStream.bufferedReader().use { it.readText() }
    }
}
