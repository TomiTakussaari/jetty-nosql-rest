package org.eclipse.jetty.nosql.rest;

public class ThreeServerJettyCluster {
    
    final EmbeddedJettyServer serverOne;
    final EmbeddedJettyServer serverTwo;
    final EmbeddedJettyServer serverThree;
    
    public ThreeServerJettyCluster() throws Exception {
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

    public void shutdown() {
        serverOne.shutdown();
        serverTwo.shutdown();
        serverThree.shutdown();
    }

}
