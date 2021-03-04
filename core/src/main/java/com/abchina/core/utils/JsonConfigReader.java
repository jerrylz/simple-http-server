package com.abchina.core.utils;

import com.abchina.core.conf.ConfigConstants;
import com.abchina.core.conf.Configuration;
import com.abchina.core.server.ServletWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abchina.core.utils.CastUtils.cast;


class JsonConfigReader {
    String readAsString(String path, Charset encoding) throws Exception {
        URI uri = ClassLoader.getSystemResource(path).toURI();
        byte[] encoded = Files.readAllBytes(Paths.get(uri));
        return new String(encoded, encoding);
    }

    Configuration parseStringAsConfiguration(String raw) throws IOException {
        Map<String, Object> rawConfig;
        ObjectMapper objectMapper = new ObjectMapper();
        rawConfig = cast(objectMapper.readValue(raw, new TypeReference<HashMap>() {
        }));
        return mapToConfiguration(rawConfig);
    }

    private Configuration mapToConfiguration(Map<String, Object> rawConfigMap) {
        Integer port = (Integer)rawConfigMap.get(ConfigConstants.PORT);
        Integer subReactorCount = (Integer)rawConfigMap.get(ConfigConstants.SUB_REACTOR_COUNT);
        Map<String, ServletWrapper> router = parseMapsToRouter(rawConfigMap);
        return new Configuration(port, subReactorCount, router);
    }

    private Map<String, ServletWrapper> parseMapsToRouter(Map<String, Object> rawConfigMap) {
        List<Map<String, Object>> maps = cast(rawConfigMap.get(ConfigConstants.ROUTER));
        Map<String, ServletWrapper> router = new HashMap<>();
        for (Map<String, Object> rkv : maps) {
            String servletName = (String) rkv.get(ConfigConstants.SERVLET_NAME);
            String path = (String) rkv.get(ConfigConstants.PATH);
            String servletClass = (String) rkv.get(ConfigConstants.SERVLET_CLASS);
            Integer loadOnStartUp = (Integer) rkv.get(ConfigConstants.LOAD_ON_STARTUP);
            ServletWrapper wrapper = new ServletWrapper(servletName,
                    path,
                    servletClass,
                    loadOnStartUp, null, null);
            router.put(path, wrapper);
        }
        return router;
    }


}
