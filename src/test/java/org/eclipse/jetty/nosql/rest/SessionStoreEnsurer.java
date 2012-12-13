package org.eclipse.jetty.nosql.rest;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jetty.nosql.rest.remote.TestHttpClient;
import org.eclipse.jetty.nosql.rest.remote.TestHttpClient.HttpResponse;

public class SessionStoreEnsurer {

    private SessionStoreEnsurer() {

    }

    public static void sessionIsSharedAndCounterUpdatedFromOneToThreeBeforeSessionIsInvalidated(ThreeServerJettyCluster jettyCluster) throws Exception {
        String url = "http://localhost:" + jettyCluster.serverOne.getPort() + "/test";
        HttpResponse response = TestHttpClient.doHttp("GET", url, null, 200, null);
        assertEquals("count = 1 value: null", response.getResponse());
        Map<String, String> cookies = response.getCookies();

        url = "http://localhost:" + jettyCluster.serverTwo.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 2 value: null", response.getResponse());

        url = "http://localhost:" + jettyCluster.serverOne.getPort() + "/test?destroy=true";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);

        /*
         * session is destroyed after request output
         */
        assertEquals("count = 3 value: null", response.getResponse());
    }

    public static void sessionKilledWhenOfOneServerShutdown(ThreeServerJettyCluster jettyCluster, String expectedResponse, Map<String, String> cookies) throws Exception {
        jettyCluster.serverOne.shutdown();

        String url = "http://localhost:" + jettyCluster.serverThree.getPort() + "/test";
        HttpResponse response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals(expectedResponse, response.getResponse());
        
    }
    

    public static Map<String, String> sessionIsSharedAndCounterUpdatedFromOneToFour(ThreeServerJettyCluster jettyCluster) throws Exception {
        String url = "http://localhost:" + jettyCluster.serverOne.getPort() + "/test";
        HttpResponse response = TestHttpClient.doHttp("GET", url, null, 200, null);
        assertEquals("count = 1 value: null", response.getResponse());

        final Map<String, String> cookies = response.getCookies();
        System.out.println("COOKIES: " + cookies);
        url = "http://localhost:" + jettyCluster.serverTwo.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 2 value: null", response.getResponse());

        url = "http://localhost:" + jettyCluster.serverThree.getPort() + "/test?value=testValue";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 3 value: testValue", response.getResponse());

        url = "http://localhost:" + jettyCluster.serverOne.getPort() + "/test";
        response = TestHttpClient.doHttp("GET", url, null, 200, cookies);
        assertEquals("count = 4 value: testValue", response.getResponse());

        return cookies;
    }

}
