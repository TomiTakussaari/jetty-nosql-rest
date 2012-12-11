package org.eclipse.jetty.nosql.rest;

import static org.junit.Assert.assertEquals;

import org.eclipse.jetty.nosql.rest.reference.ReferenceRESTStorage;
import org.eclipse.jetty.nosql.rest.remote.Constants;
import org.eclipse.jetty.nosql.rest.remote.TestHttpClient;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class SessionStorageTest {

    @Test
    public void basicSessionStoring() throws Exception {    
        SessionStoreEnsurer.sessionIsSharedAndCounterUpdatedFromOneToFour(jettyCluster);
        assertOneSessionActive();

    }

    @Test
    public void destroyingSession() throws Exception {

        SessionStoreEnsurer.sessionIsSharedAndCounterUpdatedFromOneToThreeBeforeSessionIsInvalidated(jettyCluster);
        
        assertNoSessionsActive();
        
        SessionStoreEnsurer.sessionIsSharedAndCounterUpdatedFromOneToFour(jettyCluster);

        assertOneSessionActive();
    }

    private void assertOneSessionActive() throws Exception {
        assertEquals("1", TestHttpClient.doHttp("GET", "http://localhost:" + sessionStorageServer.getPort() + "/session", null, 200, null).getResponse());
    }

    private void assertNoSessionsActive() throws Exception {
        assertEquals("0", TestHttpClient.doHttp("GET", "http://localhost:" + sessionStorageServer.getPort() + "/session", null, 200, null).getResponse());
    }


    private EmbeddedJettyServer sessionStorageServer;
    private ThreeServerJettyCluster jettyCluster;

    @Before
    public void init() throws Exception {

        ServletHolder holder = new ServletHolder(new ServletContainer(new PackagesResourceConfig(ReferenceRESTStorage.class.getPackage().getName())));
        sessionStorageServer = new EmbeddedJettyServer(0, false);

        sessionStorageServer.bindServlet(holder, "/*");
        sessionStorageServer.start();
        System.setProperty(Constants.REST_SERVER_SESSION_RESOURCE_PROPERTY, "http://localhost:"+sessionStorageServer.getPort()+"/session/sessions/");
        
        jettyCluster = new ThreeServerJettyCluster();
        
    }

    @After
    public void after() {
        jettyCluster.shutdown();
        sessionStorageServer.shutdown();
    }

}
