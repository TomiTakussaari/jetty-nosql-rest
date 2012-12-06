package org.eclipse.jetty.nosql.rest.remote;

import java.util.logging.Logger;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.rest.RestSessionManager;
import org.eclipse.jetty.nosql.rest.remote.HttpClient.HttpResponse;
import org.eclipse.jetty.nosql.rest.session.JsonSessionConverter;
import org.eclipse.jetty.nosql.rest.session.RestSession;

public class RestConnector {

    private final JsonSessionConverter sessionConverter;
    private final String sessionResource;
    private final Logger logger = Logger.getLogger(RestConnector.class.toString());
    private final RestClient restClient = new RestClient();

    public RestConnector() {
        this(System.getProperty(Constants.REST_SERVER_SESSION_RESOURCE_PROPERTY));
    }

    public RestConnector(String sessionResourcePath) {
        sessionConverter = new JsonSessionConverter();
        sessionResource = sessionResourcePath;
    }

    public boolean idInUse(String sessionId) {
        return fetchSession(sessionId) != null;
    }

    private RestSession fetchSession(String sessionId) {
        final HttpResponse response = restClient.get(sessionResource + sessionId);
        if (response == null) {
            return null;
        }

        return sessionConverter.createRestSessionFromString(response.response, response.vClock);
    }

    public NoSqlSession loadSession(String sessionId, RestSessionManager riakSessionManager, ClassLoader loader) {
        return sessionConverter.convert(fetchSession(sessionId), riakSessionManager, loader);
    }

    public void saveSession(String sessionId, NoSqlSession session, Long version) {
        restClient.post(sessionResource + sessionId, sessionConverter.createString(session, version), getVClock(session));
    }

    private String getVClock(NoSqlSession session) {
        return (String) session.getAttribute(JsonSessionConverter.REST_SESSION_VCLOCK_ID);
    }

    public void deleteSession(NoSqlSession session) {
        restClient.delete(sessionResource + session.getId(), getVClock(session));
    }

    public Object refreshSession(NoSqlSession session, Object version, ClassLoader loader) {
        final RestSession remoteSession = fetchSession(session.getId());
        if (remoteSession != null) {
            logger.info("Localversion: " + version + " remote: " + remoteSession.version + " id: " + session.getId());
            final Number localVersion = (Number) version;
            final Number remoteVersion = (Number) remoteSession.version;
            if (remoteVersion.intValue() > localVersion.intValue()) {
                sessionConverter.refreshLocalSession(session, remoteSession, loader);
                return remoteVersion;
            }
            return localVersion;
        }
        return version;
    }
}
