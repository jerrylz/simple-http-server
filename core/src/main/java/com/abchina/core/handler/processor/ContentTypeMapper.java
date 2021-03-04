package com.abchina.core.handler.processor;

import java.util.HashMap;
import java.util.Map;


public class ContentTypeMapper {

    private Map<String, String> map;

    public ContentTypeMapper() {
        this.map = new HashMap<>();
        map.put("css", "text/css");
    }

    public String get(String resource) {
        return map.get(resource);
    }

}
