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

import com.google.firebase.FirebaseApp
import ffc.airsync.api.services.MongoDbConnector
import ffc.airsync.api.services.disease.DiseaseService
import ffc.airsync.api.services.homehealthtype.HomeHealthTypeService
import ffc.airsync.api.services.specialpp.SpecialPpService
import org.eclipse.jetty.server.Server
import org.joda.time.DateTimeZone
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

internal class FFCApiServer(val args: Array<String>) {
    @Option(name = "-port", usage = "port destination ownAction start server")
    private var port = DEFAULT_PORT
    @Option(name = "-host", usage = "host destination ownAction start server")
    private var host = DEFAULT_HOST

    val logger = getLogger()

    init {
        try {
            val parser = CmdLineParser(this)
            parser.parseArgument(*args)
        } catch (cmd: CmdLineException) {
            logger.error(cmd.message)
        }
    }

    fun run() {
        logger.info("Start api parameter $args")
        logger.info("Init MogoDB connection.")
        MongoDbConnector.initialize()
        logger.info("Init firebase config.")
        FirebaseAdminSdkInit().initialize()
        logger.info("Init lookup.")
        initDiseaseAndHomeHealtyType()
        logger.info("Start main process.")
        runningProcess()
    }

    private fun runningProcess() {
        val context = ServletContextBuilder.build()
        val server = Server(JettyServerTuning.threadPool)

        server.connectors = JettyServerTuning.getConnectors(server, host, port)
        server.handler = context
        server.addBean(JettyServerTuning.getMonitor(server))
        try {
            logger.info("Start server bind port $port")
            server.start()
            logger.info("Running server main process.")
            server.join()
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    private fun initDiseaseAndHomeHealtyType() {

        Thread {
            logger.info("1:1 Disease lookup init.")
            DiseaseService.init()
            logger.info("2:2 HomeHealthTypeService lookup init.")
            HomeHealthTypeService.init()
            logger.info("3:3 SpecialPpService lookup init.")
            SpecialPpService.init()
            logger.info("Done lookup init.")
        }.start()
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
