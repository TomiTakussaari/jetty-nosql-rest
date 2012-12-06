package org.eclipse.jetty.nosql.rest.session;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.rest.RestSessionManager;
import org.eclipse.jetty.nosql.rest.session.RestSession.RestSessionAttribute;

public class JsonSessionConverter {

    private final ObjectMapper mapper;
    private final Logger logger = Logger.getLogger(JsonSessionConverter.class.toString());
    public static final String REST_SESSION_VCLOCK_ID = "REST-V-CLOCK";

    public JsonSessionConverter() {
        mapper = new ObjectMapper();
        configureMapper();
    }

    private final void configureMapper() {
        mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, true);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_CREATORS, true);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationConfig.Feature.CLOSE_CLOSEABLE, true);
        mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.configure(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, false);
        mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, false);
        mapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, true);
        mapper.setVisibilityChecker(new VisibilityChecker.Std(ANY, ANY, ANY, ANY, ANY));
    }

    public RestSession createRestSession(NoSqlSession nsSession, Object version) {
        logger.fine("Creating restSession from NoSQLSession: " + nsSession.getId());
        try {
            String vClock = null;
            Enumeration<String> attributes = nsSession.getAttributeNames();
            Map<String, RestSessionAttribute> attributeMap = new HashMap<String, RestSessionAttribute>();
            while (attributes.hasMoreElements()) {
                String key = attributes.nextElement();
                Object valueObject = nsSession.getAttribute(key);
                if (REST_SESSION_VCLOCK_ID.equals(key)) {
                    //Do not put vclock to session twice
                    vClock = (String) valueObject;
                } else {
                    serializeToSession(nsSession, attributeMap, key, valueObject);
                }
            }
            return new RestSession(nsSession.getCreationTime(), nsSession.getAccessed(), nsSession.getClusterId(), version, vClock, attributeMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void serializeToSession(NoSqlSession o, Map<String, RestSessionAttribute> attributeMap, String key, Object valueObject) throws IOException, JsonGenerationException, JsonMappingException {
        try {
            if (valueObject != null) {
                attributeMap.put(key, new RestSessionAttribute(mapper.writeValueAsString(valueObject), o.getAttribute(key).getClass().getName()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to serialize field: " + key + " value: " + valueObject);
        }
    }

    public NoSqlSession convert(RestSession riakSession, RestSessionManager riakSessionManager, ClassLoader classLoader) {
        if (riakSession == null) {
            return null;
        }
        logger.fine("Converting restSession to NoSQLSession: " + riakSession);
        try {
            NoSqlSession session = new NoSqlSession(riakSessionManager, riakSession.created, riakSession.accessed, riakSession.clusterId, riakSession.version);
            fillAttributes(riakSession, classLoader, session);
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void fillAttributes(RestSession remoteSession, ClassLoader classLoader, NoSqlSession session) throws ClassNotFoundException, IOException, JsonParseException, JsonMappingException {
        for (Entry<String, RestSessionAttribute> entry : remoteSession.attributes.entrySet()) {
            Class<?> classEntry = Class.forName(entry.getValue().className, false, classLoader);
            session.setAttribute(entry.getKey(), deSerializeJsonString(entry, classEntry));
        }
        if (remoteSession.getvClock() != null) {
            session.setAttribute(REST_SESSION_VCLOCK_ID, remoteSession.getvClock());
        }
    }

    private Object deSerializeJsonString(Entry<String, RestSessionAttribute> entry, Class<?> classEntry) throws IOException, JsonParseException, JsonMappingException {
        return mapper.readValue(entry.getValue().objectJson, classEntry);
    }

    public String createString(NoSqlSession session, Object version) {
        try {
            return mapper.writeValueAsString(createRestSession(session, version));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RestSession createRestSessionFromString(String jsonSession, String vClock) {
        logger.fine("Creating restsession from: " + jsonSession);
        try {
            RestSession rSession = mapper.readValue(jsonSession, RestSession.class);
            rSession.setvClock(vClock);
            return rSession;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshLocalSession(NoSqlSession session, RestSession rSession, ClassLoader classLoader) {
        logger.info("Refreshing session: " + session.getId());
        session.willPassivate();
        session.clearAttributes();
        try {
            fillAttributes(rSession, classLoader, session);
            session.didActivate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
