jetty-nosql-rest
================

Session manager for Jetty that can use any REST based nosql db (like Riak) for storing sessions. 

WORK IN PROGRESS, NOT READY FOR ANYTHING ELSE THAN TESTING (and may not be ready even for that)

Build
===============
git clone git://github.com/TomiTakussaari/jetty-nosql-rest.git    
cd jetty-nosql-rest    
mvn clean install    


Install & Configure
================

jetty-nosql-reset is an extension for Jetty8. 
You have to install jars into jetty's ${jetty.home}/lib/ext.

Because jetty-nosql-rest depends on few libraries (related to json serialization), you have to install also
those libraries. So it might be easiest to use jetty-nosql-rest-${version}-jar-with-dependencies.jar

Jetty requires configuration changes to both ${JETTY_HOME}/etc/jetty.xml and to ${JETTY_HOME}/context/${APP_NAME}.xml   
(or to ${APP_ROOT}/WEB-INF/jetty-web.xml)

Sample jetty.xml: (server level SessionIdManager)


      <Set name="sessionIdManager">
        <New id="restSessionIdManager" class="org.eclipse.jetty.nosql.rest.RestSessionIdManager"></New>
      </Set>

Sample jetty-web.xml: (webapplication level SessionManager)

      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New id="restSessionManaegr" class="org.eclipse.jetty.nosql.rest.RestSessionManager">
            </New>
          </Arg>
        </New>
      </Set>
      
If using embedded jetty, configuration can be done like this:

      WebAppContext context = ...;
      Server server = ...;
      RestSessionIdManager idManager = new RestSessionIdManager();
      RestSessionManager sessionManager = new RestSessionManager();
      server.setSessionIdManager(idManager);
      sessionManager.setSessionIdManager(idManager);
      SessionHandler sessionHandler = new SessionHandler();
      sessionHandler.setSessionManager(sessionManager);
      context.setSessionHandler(sessionHandler);

      
You also need to specify few system properties, configuring how Jetty connects to database:

    System.setProperty("org.eclipse.jetty.rest.server.session.resource","http://localhost:8098/riak/sessions/"); 
    
If using Riak, its also recommended to use unique clientId which will be sent in request header to db:
    
    System.setProperty("org.eclipse.jetty.rest.server.clientid","My_unique_id");

You can also configure HTTP timeouts for connections to db backend:

HttpURLConnection.connectionTimeout, default 500ms

    System.setProperty("org.eclipse.jetty.rest.server.read_timeout","500");
   
HttpURLConnection.eadTimeout, default 2000ms

    System.setProperty("org.eclipse.jetty.rest.server.connection_timeout","2000");

      
License
===============

    Copyright (c) 2012 Tomi Takussaari

All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 and Apache License v2.0 which accompanies this distribution.

The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html

The Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php

You may elect to redistribute this code under either of these licenses.
      
      
 
