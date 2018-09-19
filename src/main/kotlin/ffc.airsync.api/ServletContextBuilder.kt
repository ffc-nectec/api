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

import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

object ServletContextBuilder {

    val ROOT_PATH = ""

    fun build(): ServletContextHandler {
        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.contextPath = ROOT_PATH

        val jersey = ServletHolder(ServletContainer(ApplicationConfig()))
        jersey.initOrder = 0
        context.addServlet(jersey, "/v0/*")

        // ServletHolder holderEvents = new ServletHolder("ws-socket", SocketServlet.class);
        // holderEvents.setInitOrder(1);
        // context.addServlet(holderEvents, "/airsync/*");

        return context
    }
}
