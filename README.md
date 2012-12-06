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

Because you jetty-nosql-rest depends on few libraries (related to json serialization), you have to install also
those libraries. So it might be easiest to use jetty-nosql-rest-${version}-jar-with-dependencies.jar

Jetty requires configuration changes to both ${JETTY_HOME}/etc/jetty.xml and to ${JETTY_HOME}/context/${APP_NAME}.xml   
(or to ${APP_ROOT}/WEB-INF/jetty-web.xml)

Sample jetty.xml: (server level SessionIdManager)


      <Set name="sessionIdManager">
        <New id="restSessionIdManager" class="org.eclipse.jetty.nosql.rest.RestSessionIdManager"></New>
      </Set>

Sample jetty-web.xml: (webapplication level sessionManager)

      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New id="restSessionManaegr" class="org.eclipse.jetty.nosql.rest.RestSessionManager">
            </New>
          </Arg>
        </New>
      </Set>
      
If using embedded jetty, it is much simpler:
      WebAppContext context = ...;
      Server server = ...;
      RestSessionIdManager idManager = new RestSessionIdManager();
			RestSessionManager sessionManager = new RestSessionManager();
			server.setSessionIdManager(idManager);
			sessionManager.setSessionIdManager(idManager);
			SessionHandler sessionHandler = new SessionHandler();
			sessionHandler.setSessionManager(sessionManager);
			context.setSessionHandler(sessionHandler);

      
You also need to specify few system properties, configuring where and how jetty connects to database:
org.eclipse.jetty.rest.server.session.resource = URL for REST db, like http://localhost:8098/riak/sessions/
org.eclipse.jetty.rest.server.clientid = ClientId given to db as request header, required by Riak.

You can also configure HTTP timeouts for connections to db backend:
org.eclipse.jetty.rest.server.read_timeout = HttpURLConnection.connectionTimeout, default 500ms
org.eclipse.jetty.rest.server.connection_timeout = HttpURLConnection.eadTimeout, default 2000ms

      
      
      
      
 
