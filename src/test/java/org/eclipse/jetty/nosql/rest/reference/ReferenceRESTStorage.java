package org.eclipse.jetty.nosql.rest.reference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.jetty.nosql.rest.EmbeddedJettyServer;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.resource.Singleton;

@Consumes("application/json")
@Produces("application/json")
@Singleton
@Path("/session")
public class ReferenceRESTStorage {

    private final Map<String, String> restSessions = Collections.synchronizedMap(new HashMap<String, String>());

    @Path("/sessions/{sessionId}")
    @PUT
    public void put(@PathParam("sessionId") String sessionId, String data) {
        restSessions.put(sessionId, data);
    }

    @Path("/sessions/{sessionId}")
    @POST
    public void post(@PathParam("sessionId") String sessionId, String data) {
        System.out.println("POST SessionId: " + sessionId + "IS "+restSessions.get(sessionId));
        restSessions.put(sessionId, data);
    }

    @Path("/sessions/{sessionId}")
    @GET
    public String get(@PathParam("sessionId") String sessionId) {
        if (restSessions.containsKey(sessionId)) {
            System.out.println("GET SessionId: " + sessionId + "IS "+restSessions.get(sessionId));
            return restSessions.get(sessionId);
        } else {
            throw new NotFoundException();
        }

    }

    @Path("/sessions/{sessionId}")
    @DELETE
    public void delete(@PathParam("sessionId") String sessionId) {
        if (restSessions.containsKey(sessionId)) {
            restSessions.remove(sessionId);
        } else {
            throw new NotFoundException();
        }
    }

    @GET
    public String getSessionCount() {
        return restSessions.size() + "";
    }

    public static void main(String... args) throws Exception {
        EmbeddedJettyServer sessionStorageServer = new EmbeddedJettyServer(0, false);
        ServletHolder holder = new ServletHolder(new ServletContainer(new PackagesResourceConfig(ReferenceRESTStorage.class.getPackage().getName())));
        sessionStorageServer.bindServlet(holder, "/*");
        sessionStorageServer.start();

    }

}
