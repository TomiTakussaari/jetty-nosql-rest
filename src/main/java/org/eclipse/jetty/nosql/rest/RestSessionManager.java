package org.eclipse.jetty.nosql.rest;

import java.util.logging.Logger;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.NoSqlSessionManager;
import org.eclipse.jetty.nosql.rest.remote.RestConnector;

public class RestSessionManager extends NoSqlSessionManager {

    private final Logger log = Logger.getLogger(RestSessionIdManager.class.toString());
    private RestConnector db;

    @Override
    public void doStart() throws Exception {
        super.doStart();
        db = new RestConnector();
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
        session.willPassivate();
        db.deleteSession(session);
        session.didActivate();
        return true;
    }

    @Override
    protected Object save(NoSqlSession session, Object localVersion, boolean activateAfterSave) {
        log.info("save:" + session + " ID:" + session.getId());
        Long version = getVersion(localVersion);
        if (session.isValid()) {
            session.willPassivate();

            db.saveSession(session.getId(), session, version);

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
