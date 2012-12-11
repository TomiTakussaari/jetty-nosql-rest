package org.eclipse.jetty.nosql.rest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.nosql.rest.remote.RestConnector;
import org.eclipse.jetty.server.session.AbstractSessionIdManager;

public class RestSessionIdManager extends AbstractSessionIdManager {

    private final Set<String> _sessions = Collections.synchronizedSet(new LinkedHashSet<String>());
    private final Logger log = Logger.getLogger(RestSessionIdManager.class.toString());
    private final RestConnector db = new RestConnector();

    @Override
    public void addSession(HttpSession session) {
        if (session == null) {
            return;
        }
        log.fine("addSession:" + session.getId());

        _sessions.add(session.getId());
    }

    @Override
    public String getClusterId(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        int dot = nodeId.lastIndexOf('.');
        return (dot > 0) ? nodeId.substring(0, dot) : nodeId;
    }

    @Override
    public String getNodeId(String clusterId, HttpServletRequest arg1) {
        if (clusterId == null) {
            return null;
        }
        if (_workerName != null) {
            return clusterId + '.' + _workerName;
        }
        return clusterId;
    }

    @Override
    public boolean idInUse(String sessionId) {
        synchronized (this) {
            boolean inUse = db.idInUse(sessionId);
            log.info("Session " + sessionId + " in use: " + inUse);
            return inUse;
        }
    }

    @Override
    public void invalidateAll(String sessionId) {
        _sessions.remove(sessionId);
    }

    @Override
    public void removeSession(HttpSession session) {
        if (session == null) {
            return;
        }
        _sessions.remove(session.getId());
    }

}
