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

package ffc.airsync.api;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import th.in.ffc.airsync.api.services.SocketServlet;

public class ServletContextBuilder {

    public static final String ROOT_PATH = "";

    public static ServletContextHandler build() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(ROOT_PATH);

        ServletHolder jersey = new ServletHolder(new ServletContainer(new ApplicationConfig()));
        jersey.setInitOrder(0);
        context.addServlet(jersey, "/v0/*");

        ServletHolder holderEvents = new ServletHolder("ws-socket", SocketServlet.class);
        //holderEvents.setInitOrder(1);
        context.addServlet(holderEvents, "/airsync/*");

        return context;
    }

}

