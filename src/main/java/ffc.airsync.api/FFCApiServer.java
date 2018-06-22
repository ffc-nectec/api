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

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FFCApiServer {


    protected static final int DEFAULT_PORT = 8080;
    protected static final String DEFAULT_HOST = "0.0.0.0";
    public static FirebaseApp firebaseApp = null;
    private static FFCApiServer instance;
    @Option(name = "-dev", usage = "mode")
    protected boolean dev = false;
    @Option(name = "-port", usage = "port destination ownAction start server")
    protected int port = DEFAULT_PORT;
    @Option(name = "-host", usage = "port destination ownAction start server")
    protected String host = DEFAULT_HOST;

    public FFCApiServer(String[] args) {
        try {
            CmdLineParser parser = new CmdLineParser(this);
            parser.parseArgument(args);
        } catch (CmdLineException cmd) {
            cmd.printStackTrace();
        }
    }

    public static void main(String[] args) {
        instance = new FFCApiServer(args);
        instance.run();
    }


    private void run() {


        try {
            FileInputStream serviceAccount =
                    new FileInputStream("D:\\workspace\\airsync\\airsync-api\\src\\main\\java\\ffc\\airsync\\api\\ffc-nectec-firebase-adminsdk-4ogjg-88a2843d02.json");


            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://ffc-nectec.firebaseio.com")
                    .build();
            firebaseApp = FirebaseApp.initializeApp(options);
            //logger.log(Level.FINE, "Load config firebase from file.");
        } catch (IOException e) {

            e.printStackTrace();

            String firebaseConfigString = System.getenv("FIREBASE_CONFIG");
            byte byteFirebaseConfig[] = firebaseConfigString.getBytes();
            ByteArrayInputStream streamFirebaseConfig = new ByteArrayInputStream(byteFirebaseConfig);

            FirebaseOptions options = null;
            try {
                options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(streamFirebaseConfig))
                        .setDatabaseUrl("https://ffc-nectec.firebaseio.com")
                        .build();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            firebaseApp = FirebaseApp.initializeApp(options);
            //logger.log(Level.FINE, "Load config firebase from system env.");
        }


        System.out.println("Start main process");
        ServletContextHandler context = ServletContextBuilder.build();

        Server server = new Server(JettyServerTuning.getThreadPool());

        server.setConnectors(JettyServerTuning.getConnectors(server, host, port));
        server.setHandler(context);
        server.addBean(JettyServerTuning.getMonitor(server));
        try {
            System.out.println("Start server bind port " + port);
            server.start();
            System.out.println("Running process");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
