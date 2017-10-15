package org.playstat.crawler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.playstat.agent.HTTPRequest;
import org.playstat.agent.HTTPResponse;
import org.playstat.agent.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCache implements ICache {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String DEFAULT_CACHE_FOLDER = System.getProperty("user.home") + File.separator + ".parser"
            + File.separator + "cache" + File.separator;
    private final File cacheFolder;

    public FileCache() {
        this(DEFAULT_CACHE_FOLDER);
    }

    public FileCache(String cachePath) {
        this.cacheFolder = prepareFS(cachePath);
    }

    @Override
    public File getCacheFile(HTTPRequest request) {
        try {
            final String url = request.getUrl();
            // TODO: normalize url
            final String host = new URL(url).getHost();
            String filename = MD5(url);
            final String subFolder = host + File.separator + filename.substring(0, 2) + File.separator
                    + filename.substring(0, 4);
            filename = subFolder + File.separator + filename;

            new File(cacheFolder.getAbsolutePath() + File.separator + subFolder).mkdirs();

            return new File(cacheFolder.getAbsolutePath() + File.separator + filename);

        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static File prepareFS(String cachePath) {
        final File cf = new File(cachePath);
        if (!cf.exists()) {
            cf.mkdirs();
        }
        return cf;
    }

    private String MD5(String md5) {
        try {
            final java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            final byte[] array = md.digest(md5.getBytes());
            final StringBuilder sb = new StringBuilder();
            for (final byte element : array) {
                sb.append(Integer.toHexString(element & 0xFF | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final java.security.NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean isCahed(HTTPRequest request) {
        return getCacheFile(request).exists();
    }

    @Override
    public Transaction get(HTTPRequest request) throws IOException {
        if (!isCahed(request)) {
            return null;
        }
        final Transaction result = new Transaction(request);
        final File contentFile = getCacheFile(request);
        final CacheMetaData meta = new CacheMetaData();
        final Map<String, List<String>> header = meta.getHeader();
        final int responseCode = meta.getResponseCode();
        final HTTPResponse response = new HTTPResponse(responseCode, header,
                new ByteArrayInputStream(java.nio.file.Files.readAllBytes(contentFile.toPath())));
        result.setResponse(response);
        return result;
    }

    @Override
    public void cache(Transaction t) {
        t.getRequest();
        t.getResponse();
        // TODO Auto-generated method stub
    }
}
