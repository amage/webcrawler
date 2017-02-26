package org.playstat.crawler.cache;

import java.io.File;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.playstat.agent.HTTPRequest;
import org.playstat.crawler.cache.ICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCache implements ICache {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String CACHE_FOLDER = String.join(File.separator, System.getProperty("user.home"), ".parser", "cache") + File.separator;

    public FileCache() {
        log.debug("Cache folder: " + CACHE_FOLDER);
    }

    @Override
    public File getCacheFile(HTTPRequest request) {
        try {
            final String path = getPath(request.getUrl());
            final String subFolder = new File(path).getParent();

            final File cacheFolder = prepareFS();
            new File(cacheFolder.getAbsolutePath() + File.separator + subFolder).mkdirs();

            return new File(cacheFolder.getAbsolutePath() + File.separator + path);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isChached(HTTPRequest request) {
        try {
            return new File(getPath(request.getUrl())).exists();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private static String getPath(String url) throws Exception {
        final String filename = MD5(url);

        final String subFolder = String.join(File.separator, new URL(url).getHost(),
                filename.substring(0, 2), filename.substring(0, 4));

        return subFolder + File.separator + filename;
    }

    private static File prepareFS() {
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        return cacheFolder;
    }

    private static String MD5(String md5) throws NoSuchAlgorithmException {
        final MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        final byte[] array = md.digest(md5.getBytes());
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }
}
