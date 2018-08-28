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

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.LowResourceMonitor
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool

internal object JettyServerTuning {

    private val MAX_THREADS = 500
    private val MIN_THREADS = 50
    private val IDLE_TIMEOUT = 6000

    val threadPool: QueuedThreadPool
        get() {
            val threadPool = QueuedThreadPool(MAX_THREADS, MIN_THREADS, IDLE_TIMEOUT)
            threadPool.isDaemon = true
            threadPool.isDetailedDump = false
            return threadPool
        }

    fun getMonitor(server: Server): LowResourceMonitor {
        val monitor = LowResourceMonitor(server)
        monitor.period = 1000
        monitor.lowResourcesIdleTimeout = 1000
        monitor.monitorThreads = true
        monitor.maxConnections = 0
        monitor.maxMemory = 0
        monitor.maxLowResourcesTime = 5000
        return monitor
    }

    fun getConnectors(server: Server, host: String, port: Int): Array<Connector> {
        val connector = ServerConnector(server)
        connector.host = host
        connector.port = port
        connector.idleTimeout = 30000
        connector.acceptQueueSize = 3000
        return arrayOf(connector)
    }
}