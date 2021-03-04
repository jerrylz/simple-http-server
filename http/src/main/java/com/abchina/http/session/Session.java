package com.abchina.http.session;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private String sessionId;
    private Map<String, Object> mapper = new HashMap<>();

    public void put(String key, Object value) {
        mapper.put(key, value);
    }

    public Object get(String key) {
        return mapper.get(key);
    }

    public Session(String sessionId) {

        this.sessionId = sessionId;
    }
}
