package org.playstat.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.playstat.agent.ICaptchaSolver;
import org.playstat.agent.WebClientAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private boolean cacheEnable = true;
    private final String CACHE_FOLDER = System.getProperty("user.home")
            + File.separator + ".parser" + File.separator + "cache"
            + File.separator;
    private final WebClientAgent web;
    private String charsetName = "UTF-8";
    private String baseUrl = "";
    private ICaptchaSolver captchaSolver;

    public WebClient(WebClientAgent web) {
        this.web = web;
    }

    public Document go(String url) throws IOException {
        return go(url, baseUrl);
    }

    public InputStream post(String url, Map<String, String> params)
            throws IOException {
        return web.getAgent().post(new URL(url), params);
    }

    public InputStream goRaw(String url) throws IOException {
        this.setBaseUrl(baseUrl);
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        String filename = MD5(url);
        final String subFolder = filename.substring(0, 2) + File.separator
                + filename.substring(0, 4);
        filename = subFolder + File.separator + filename;
        new File(cacheFolder.getAbsolutePath() + File.separator + subFolder)
                .mkdirs();
        File pageFile = new File(cacheFolder.getAbsolutePath() + File.separator
                + filename);
        try {
            if (pageFile.exists() && cacheEnable) {
                return new FileInputStream(pageFile);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Files.copy(getWebAgent().go(url), pageFile.toPath());
        return new FileInputStream(pageFile);
    }

    // TODO: rewrite cleaner
    public Document go(String url, String baseUrl) throws IOException {
        this.setBaseUrl(baseUrl);
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        String filename = MD5(url);
        final String subFolder = filename.substring(0, 2) + File.separator
                + filename.substring(0, 4);
        filename = subFolder + File.separator + filename;
        new File(cacheFolder.getAbsolutePath() + File.separator + subFolder)
                .mkdirs();
        File pageFile = new File(cacheFolder.getAbsolutePath() + File.separator
                + filename);
        Document result = null;
        try {
            if (pageFile.exists() && cacheEnable) {
                result = Jsoup.parse(pageFile, charsetName, baseUrl);
                return result;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        result = Jsoup.parse(getWebAgent().go(url), getCharsetName(), baseUrl);
        if (getCaptchaSolver() != null) {
            if (getCaptchaSolver().isCaptchaPage(result)) {
                getCaptchaSolver().solve(this, result);
                return go(url, baseUrl);
            }
        }
        FileOutputStream out = new FileOutputStream(pageFile);
        out.write(result.html().getBytes(charsetName));
        out.close();
        return result;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        web.getAgent().setCharset(charsetName);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public void download(String url, String filename) throws IOException {
        final File outFile = new File(filename);
        outFile.getParentFile().mkdirs();

        logger.trace("downloading file: " + url);
        ReadableByteChannel rbc = Channels.newChannel(getWebAgent().go(url));
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    public WebClientAgent getWebAgent() {
        return web;
    }

    public ICaptchaSolver getCaptchaSolver() {
        return captchaSolver;
    }

    public void setCaptchaSolver(ICaptchaSolver captchaSolver) {
        this.captchaSolver = captchaSolver;
    }
}
