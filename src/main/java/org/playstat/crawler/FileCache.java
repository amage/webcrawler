package org.playstat.crawler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.playstat.agent.HTTPRequest;

public class FileCache implements ICache {
    private static final String CACHE_FOLDER = System.getProperty("user.home") + File.separator + ".parser" + File.separator
            + "cache" + File.separator;

    @Override
    public File getCacheFile(HTTPRequest request) {
        try {
            String url = request.getUrl();
			final String host = new URL(url).getHost();
            String filename = MD5(url);
            final File cacheFolder = prepareFS();
            final String subFolder = host + File.separator + filename.substring(0, 2) + File.separator + filename.substring(0, 4);
            filename = subFolder + File.separator + filename;

            new File(cacheFolder.getAbsolutePath() + File.separator + subFolder).mkdirs();

            return new File(cacheFolder.getAbsolutePath() + File.separator + filename);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private File prepareFS() {
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        return cacheFolder;
    }

    private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // TODO logger
            e.printStackTrace();
        }
        return null;
    }
}
