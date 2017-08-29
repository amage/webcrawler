package org.playstat.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO write tests
public class URLUtils {

    public static Map<String, List<String>> parseParams(String url) {
        final Map<String, List<String>> result = new HashMap<>();
        final String params = url.substring(url.indexOf("?"));
        Arrays.asList(params.split("&")).forEach(param -> {
            final String key = param.contains("=") ? param.substring(0, param.indexOf("=")) : param;
            final String value = param.contains("=") ? param.substring(param.indexOf("=") + 1) : param;
            final List<String> values = result.containsKey(key) ? result.get(key) : result.put(key, new ArrayList<>());
            if (!values.contains(value)) {
                values.add(value);
            }
        });
        return result;
    }
}
