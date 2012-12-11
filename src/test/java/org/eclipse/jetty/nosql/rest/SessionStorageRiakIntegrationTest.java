package org.eclipse.jetty.nosql.rest;

import org.eclipse.jetty.nosql.rest.remote.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SessionStorageRiakIntegrationTest {
    
    private ThreeServerJettyCluster jettyCluster;

    @Before
    public void init() throws Exception {
        
        if(System.getProperty(Constants.REST_SERVER_SESSION_RESOURCE_PROPERTY) == null) {
            throw new RuntimeException("No riak backend defined, specify it in systemproperty "+Constants.REST_SERVER_SESSION_RESOURCE_PROPERTY+" like: http://localhost:8098/riak/sessions/");
        }

        jettyCluster = new ThreeServerJettyCluster();
    }

    @After
    public void after() {
        jettyCluster.shutdown();
    }
    
    @Test
    public void sessionSharing() throws Exception {
        SessionStoreEnsurer.sessionIsSharedAndCounterUpdatedFromOneToFour(jettyCluster);
    }
    
    @Test
    public void destroyingSession() throws Exception {
        SessionStoreEnsurer.sessionIsSharedAndCounterUpdatedFromOneToThreeBeforeSessionIsInvalidated(jettyCluster);
        
        SessionStoreEnsurer.sessionIsSharedAndCounterUpdatedFromOneToFour(jettyCluster);
    }

}
