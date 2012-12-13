package org.eclipse.jetty.nosql.rest.reference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

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

    private final Map<String, ValueWithVclock> restSessions = Collections.synchronizedMap(new HashMap<String, ValueWithVclock>());

    private static class ValueWithVclock {
        ValueWithVclock(String data, String header) {
            this.value = data;
            this.vclock = header;
        }

        String value;
        String vclock;

    }

    @Path("/sessions/{sessionId}")
    @PUT
    public void put(@PathParam("sessionId") String sessionId, String data, @Context HttpServletRequest request) {
        if (restSessions.containsKey(sessionId)) {
            putWithVclock(sessionId, data, request);
        } else {
            System.out.println("NOTE, Session already exists: "+sessionId);
            post(sessionId, data);
        }

    }

    private void putWithVclock(String sessionId, String data, HttpServletRequest request) {
        String vclock = request.getHeader("X-Riak-Vclock");

        if (vclock == null) {
            vclock = "1";
            System.out.println("WARNING, NO VCLOCK SPECIFIED!");
        }
        ValueWithVclock vwc = new ValueWithVclock(data, plusByOne(vclock));
        restSessions.put(sessionId, vwc);
    }

    @Path("/sessions/{sessionId}")
    @POST
    public void post(@PathParam("sessionId") String sessionId, String data) {
        if (restSessions.containsKey(sessionId)) {
            throw new RuntimeException("Key already exists!");
        } else {
            restSessions.put(sessionId, new ValueWithVclock(data, "1"));
        }
    }

    @Path("/sessions/{sessionId}")
    @GET
    public String get(@PathParam("sessionId") String sessionId, @Context HttpServletResponse response) {
        if (restSessions.containsKey(sessionId)) {
            ValueWithVclock vwc = restSessions.get(sessionId);
            response.setHeader("X-Riak-Vclock", vwc.vclock);
            return vwc.value;
        } else {
            throw new NotFoundException();
        }

    }

    private String plusByOne(String vclock) {
        Integer i = Integer.valueOf(vclock);
        i++;
        return i.toString();
    }

    @Path("/sessions/{sessionId}")
    @DELETE
    public void delete(@PathParam("sessionId") String sessionId, @Context HttpServletRequest request) {
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
