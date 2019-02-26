package ffc.airsync.api.services.image

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import java.io.InputStream

private val MEDIA_TYPE = okhttp3.MediaType.parse("multipart/form-data")

internal fun postToImageServer(url: String, body: RequestBody): Response {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    client.newCall(request).execute().use { response -> return response }
}

internal fun createRequestStream(file: InputStream): RequestBody {
    return object : RequestBody() {
        override fun contentType(): okhttp3.MediaType? {
            return MEDIA_TYPE
        }

        override fun writeTo(sink: BufferedSink) {
            try {
                val buffer = ByteArray(1024)
                var length: Int
                length = file.read(buffer)

                while (length > 0) {
                    sink.write(buffer, 0, length)
                    length = file.read(buffer)
                }
            } finally {
                file.close()
            }
        }
    }
}
