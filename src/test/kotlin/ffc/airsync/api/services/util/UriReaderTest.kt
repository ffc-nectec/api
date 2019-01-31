package ffc.airsync.api.services.util

import org.amshove.kluent.`should contain`
import org.junit.Ignore
import org.junit.Test
import java.net.URI

class UriReaderTest {

    @Test
    fun readUrl() {
        val reader = UriReader(URI("https://raw.githubusercontent.com/ffc-nectec/entities/master/README.md"))
        val text = reader.readAsString()
        text `should contain` "FFC"
    }

    @Ignore
    @Test
    fun readFileUri() {
        val reader = UriReader(URI("file:///C:/Windows/WindowsUpdate.log"))
        val text = reader.readAsString()
        println(text)
    }
}
