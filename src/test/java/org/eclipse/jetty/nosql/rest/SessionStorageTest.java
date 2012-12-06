package org.eclipse.jetty.nosql.rest;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jetty.nosql.rest.reference.ReferenceRESTStorage;
import org.eclipse.jetty.nosql.rest.remote.Constants;
import org.eclipse.jetty.nosql.rest.remote.TestHttpClient;
import org.eclipse.jetty.nosql.rest.remote.TestHttpClient.HttpResponse;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class SessionStorageTest {

    @Test
    public void basicSessionStoring() throws Exception {
        String url = "http://localhost:" + serverOne.getPort() + "/test";
        HttpResponse response = TestHttpClient.doHttp("GET", url, null, 200, null);
        assertEquals("count = 1 value: null", response.getResponse());

        final Map<String, String> cookies = response.getCookies();
        System.out.println("COOKIES: " + cookies);
        url = "http://localhost:" + serverTwo.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 2 value: null", response.getResponse());

        url = "http://localhost:" + serverThree.getPort() + "/test?value=testValue";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 3 value: testValue", response.getResponse());

        url = "http://localhost:" + serverOne.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 4 value: testValue", response.getResponse());
        assertOneSessionActive();

    }

    @Test
    public void destroyingSession() throws Exception {
        String url = "http://localhost:" + serverOne.getPort() + "/test";
        HttpResponse response = TestHttpClient.doHttp("GET", url, null, 200, null);
        assertEquals("count = 1 value: null", response.getResponse());
        Map<String, String> cookies = response.getCookies();

        url = "http://localhost:" + serverTwo.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 2 value: null", response.getResponse());

        url = "http://localhost:" + serverOne.getPort() + "/test?destroy=true";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        
        /*
         * session is destroyed after request output
         */
        assertEquals("count = 3 value: null", response.getResponse()); 

        assertNoSessionsActive();

        url = "http://localhost:" + serverTwo.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, response.getCookies());
        assertEquals("count = 1 value: null", response.getResponse());

        url = "http://localhost:" + serverOne.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, response.getCookies());
        assertEquals("count = 2 value: null", response.getResponse());

        assertOneSessionActive();

    }

    private void assertOneSessionActive() throws Exception {
        assertEquals("1", TestHttpClient.doHttp("GET", "http://localhost:" + sessionStorageServer.getPort() + "/session", null, 200, null).getResponse());
    }

    private void assertNoSessionsActive() throws Exception {
        assertEquals("0", TestHttpClient.doHttp("GET", "http://localhost:" + sessionStorageServer.getPort() + "/session", null, 200, null).getResponse());
    }

    private EmbeddedJettyServer serverOne;
    private EmbeddedJettyServer serverTwo;
    private EmbeddedJettyServer serverThree;
    private EmbeddedJettyServer sessionStorageServer;

    @Before
    public void init() throws Exception {

        ServletHolder holder = new ServletHolder(new ServletContainer(new PackagesResourceConfig(ReferenceRESTStorage.class.getPackage().getName())));
        sessionStorageServer = new EmbeddedJettyServer(0, false);

        sessionStorageServer.bindServlet(holder, "/*");
        sessionStorageServer.start();
        System.setProperty(Constants.REST_SERVER_SESSION_RESOURCE_PROPERTY, "http://localhost:"+sessionStorageServer.getPort()+"/session/sessions/");
        serverOne = new EmbeddedJettyServer(0, true);
        serverOne.bindServlet(TestServlet.class, "/test");
        serverTwo = new EmbeddedJettyServer(0, true);
        serverTwo.bindServlet(TestServlet.class, "/test");
        serverThree = new EmbeddedJettyServer(0, true);
        serverThree.bindServlet(TestServlet.class, "/test");
        serverOne.start();
        serverTwo.start();
        serverThree.start();
    }

    public void after() {
        serverOne.shutdown();
        serverTwo.shutdown();
        serverThree.shutdown();
        sessionStorageServer.shutdown();
    }

}
