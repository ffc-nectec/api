/*
 * Copyright (c) 2561 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.airsync.api

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import ffc.airsync.api.services.disease.DiseaseService
import ffc.airsync.api.services.homehealthtype.HomeHealthTypeService
import ffc.airsync.api.services.specialpp.SpecialPpService
import org.eclipse.jetty.server.Server
import org.joda.time.DateTimeZone
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

internal class FFCApiServer(args: Array<String>) {
    @Option(name = "-dev", usage = "mode")
    protected var dev = false
    @Option(name = "-port", usage = "port destination ownAction start server")
    protected var port = DEFAULT_PORT
    @Option(name = "-host", usage = "port destination ownAction start server")
    protected var host = DEFAULT_HOST

    init {
        try {
            val parser = CmdLineParser(this)
            parser.parseArgument(*args)
        } catch (cmd: CmdLineException) {
            cmd.printStackTrace()
        }
    }

    fun run() {
        getFirebaseParameter()
        initDiseaseAndHomeHealtyType()
        runningProcess()
    }

    private fun runningProcess() {
        println("Start main process")
        val context = ServletContextBuilder.build()
        val server = Server(JettyServerTuning.threadPool)

        server.connectors = JettyServerTuning.getConnectors(server, host, port)
        server.handler = context
        server.addBean(JettyServerTuning.getMonitor(server))
        try {
            println("Start server bind port $port")
            server.start()
            println("Running process")
            server.join()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initDiseaseAndHomeHealtyType() {
        Thread {
            println("1:3 Disease init.")
            DiseaseService.init()
            println("2:3 HomeHealthTypeService init.")
            HomeHealthTypeService.init()
            println("3:3 SpecialPpService init.")
            SpecialPpService.init()
            println("Done init.")
        }.start()
    }

    private fun getFirebaseParameter() {
        try {
            val serviceAccount =
                FileInputStream("ffc-nectec-firebase-adminsdk-4ogjg-88a2843d02.json")
            val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://ffc-nectec.firebaseio.com")
                .build()
            firebaseApp = FirebaseApp.initializeApp(options)
            // logger.log(Level.FINE, "Load config firebase from file.");
        } catch (e: IOException) {
            e.printStackTrace()
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
                e1.printStackTrace()
            }

            firebaseApp = FirebaseApp.initializeApp(options!!)
            // logger.log(Level.FINE, "Load config firebase from system env.");
        }
    }

    companion object {
        protected val DEFAULT_PORT = 8080
        protected val DEFAULT_HOST = "0.0.0.0"
        var firebaseApp: FirebaseApp? = null
        var instance: FFCApiServer? = null
    }
}

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(7))))
    DateTimeZone.setDefault(DateTimeZone.forOffsetHours(7))
    FFCApiServer.instance = FFCApiServer(args)
    FFCApiServer.instance!!.run()
}
