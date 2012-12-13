package org.eclipse.jetty.nosql.rest.session;

import java.io.Serializable;
import java.util.Map;

public class RestSession implements Serializable {
    private static final long serialVersionUID = 7633249262408596151L;
    public final long created;
    public final long accessed;
    public final String clusterId;
    public final Object version;
    private String vClock;
    public final Map<String, RestSessionAttribute> attributes;

    public RestSession(long created, long accessed, String clusterId, Object version, String vClock, Map<String, RestSessionAttribute> attributes) {
        this.created = created;
        this.accessed = accessed;
        this.clusterId = clusterId;
        this.version = version;
        this.vClock = vClock;
        this.attributes = attributes;
    }

    RestSession() {
        this(0, 0, null, null, null, null);
        // For serialization
    }

    public String getvClock() {
        return vClock;
    }

    public void setvClock(String vClock) {
        this.vClock = vClock;
    }

    public static class RestSessionAttribute {
        public RestSessionAttribute() {
            this(null, null);
            // For serialization
        }

        public RestSessionAttribute(String value, String className) {
            this.objectJson = value;
            this.className = className;
        }

        public final String objectJson;
        public final String className;
    }
}
