package org.eclipse.jetty.nosql.rest;

import java.util.logging.Logger;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.NoSqlSessionManager;
import org.eclipse.jetty.nosql.rest.remote.RestConnector;

public class RestSessionManager extends NoSqlSessionManager {

    private final Logger log = Logger.getLogger(RestSessionIdManager.class.toString());
    private final RestConnector db = new RestConnector();
    
    public RestSessionManager() {
        setStalePeriod(0);
    }

    @Override
    protected NoSqlSession loadSession(String sessionId) {
        log.info("load:" + sessionId);
        return db.loadSession(sessionId, this, getContext().getContextHandler().getClassLoader());
    }

    @Override
    protected Object refresh(NoSqlSession session, Object version) {
        log.info("refresh:" + session + " ID:" + session.getId());
        version = db.refreshSession(session, version, getContext().getContextHandler().getClassLoader());
        return version;
    }

    @Override
    protected boolean remove(NoSqlSession session) {
        log.info("remove:" + session + " ID:" + session.getId() + " CLUSTER " + session.getClusterId());
        db.deleteSession(session);
        return true;
    }

    @Override
    protected Object save(NoSqlSession session, Object localVersion, boolean activateAfterSave) {
        log.info("save:" + session + " ID:" + session.getId());
        final Long version = getVersion(localVersion);
        if (session.isValid() && session.isDirty()) {
            session.takeDirty();
            log.info("Saving session, it was dirty.");

            session.willPassivate();
            
            db.saveSession(session.getId(), session, version, getContext().getContextHandler().getClassLoader());

            if (activateAfterSave) {
                session.didActivate();
            }
        }
        return version;
    }

    private Long getVersion(Object version) {
        if (version == null) {
            return new Long(1);
        } else if (version instanceof Long) {
            return new Long(((Long) version).intValue() + 1);
        } else {
            return new Long(((Number) version).intValue() + 1);
        }
    }

}
