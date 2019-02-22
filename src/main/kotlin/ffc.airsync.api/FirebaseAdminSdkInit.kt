package ffc.airsync.api

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException

class FirebaseAdminSdkInit {
    val logger = getLogger()

    fun initialize() {
        var res = kotlin.runCatching { firebaseAdminFromjsonFile() }
        if (res.exceptionOrNull() is IOException) {
            res = kotlin.runCatching { firebaseAdminFromSystemEnv() }
        }
        if (res.isFailure) logger.error("Firebase Error", res.exceptionOrNull())
    }

    private fun firebaseAdminFromjsonFile() {
        val serviceAccount =
            FileInputStream("ffc-nectec-firebase-adminsdk-4ogjg-88a2843d02.json")
        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://ffc-nectec.firebaseio.com")
            .build()
        FFCApiServer.firebaseApp = FirebaseApp.initializeApp(options)
        logger.debug("Load firebase config from file.")
    }

    private fun firebaseAdminFromSystemEnv() {
        logger.debug("Load firebase config from system env FIREBASE_CONFIG")
        val firebaseConfigString = System.getenv("FIREBASE_CONFIG")
        val byteFirebaseConfig = firebaseConfigString.toByteArray()
        val streamFirebaseConfig = ByteArrayInputStream(byteFirebaseConfig)
        var options: FirebaseOptions? = null
        try {
            options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(streamFirebaseConfig))
                .setDatabaseUrl("https://ffc-nectec.firebaseio.com")
                .build()
        } catch (e1: IOException) {
            logger.info("Cannot load filebase config. ${e1.message}", e1)
        }

        FFCApiServer.firebaseApp = FirebaseApp.initializeApp(options!!)
        // logger.log(Level.FINE, "Load config firebase from system env.");
    }
}
