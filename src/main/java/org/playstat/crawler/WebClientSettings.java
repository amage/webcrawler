package org.playstat.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class WebClientSettings {

    private final static Logger log = LoggerFactory.getLogger(WebClientSettings.class);
    private String defaultCharsetName = "UTF-8";
    private String baseUrl = "";
    private boolean cacheEnable = true;
    private List<String> languages = new ArrayList<>(Arrays.asList("ru", "en"));

    private WebClientSettings() {
    }

    public static WebClientSettings fromFile(String path) {
        final Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(new File(path))) {
            p.load(in);
            final WebClientSettings result = new WebClientSettings();
            result.setDefaultCharsetName(p.getProperty("defaultCharsetName"));
            result.setBaseUrl(p.getProperty("baseUrl"));
            result.setCacheEnable(Boolean.parseBoolean(p.getProperty("cacheEnable")));
            return result;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String getDefaultCharsetName() {
        return defaultCharsetName;
    }

    public void setDefaultCharsetName(String defaultCharsetName) {
        this.defaultCharsetName = defaultCharsetName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public static WebClientSettings defs() {
        // Initialization order:
        // 0. Class defaults.
        // TODO:
        // 1. Look for global configuration.
        // 2. Look for user's configuration.
        // 3. Look in application default path.
        return new WebClientSettings();
    }

}
