package org.playstat.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.playstat.agent.ICaptchaSolver;
import org.playstat.agent.WebClientAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebClient {
    private static final int TRY_TIMES = 1024;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private boolean cacheEnable = true;
    private final String CACHE_FOLDER = System.getProperty("user.home") + File.separator + ".parser" + File.separator + "cache" + File.separator;
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

    public InputStream post(String url, Map<String, String> params) throws IOException {
        return web.getAgent().post(new URL(url), params);
    }

    // TODO: rewrite cleaner
    public Document go(String url, String baseUrl) throws IOException {
        this.setBaseUrl(baseUrl);
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        String filename = MD5(url);
        final String subFolder = filename.substring(0, 2) + File.separator + filename.substring(0, 4);
        filename = subFolder + File.separator + filename;
        new File(cacheFolder.getAbsolutePath() + File.separator + subFolder).mkdirs();
        File pageFile = new File(cacheFolder.getAbsolutePath() + File.separator + filename);
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
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
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

        logger.trace("downloading image: " + url);
        for (int i = 0; i < TRY_TIMES; i++) {
            try {
                ReadableByteChannel rbc = Channels.newChannel(getWebAgent().go(url));
                FileOutputStream fos = new FileOutputStream(new File(outFile + ".tmp"));
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                try {
                    final File imgFile = new File(filename + ".tmp");
                    checkImage(imgFile);
                    imgFile.renameTo(new File(filename));
                    logger.trace("image downloaded: " + url);
                    return;
                } catch (Exception e) {
                    logger.warn("image corrupted: " + e.getMessage() + " try again " + i + "/" + (TRY_TIMES - 1));
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    i = TRY_TIMES;
                }
                logger.warn("image download fail: " + e.getMessage() + " try again " + i + "/" + (TRY_TIMES - 1));
            }
        }
        throw new RuntimeException("Faild to download image");
    }

    private void checkImage(final File imgFile) throws IOException {
        BufferedImage image = ImageIO.read(imgFile);
        int[] array = null;
        array = image.getData().getPixels(0, image.getHeight() - 1, image.getWidth(), 1, array);
        for (int i : array) {
            if (i != 128) {
                return;
            }
        }
        throw new IOException("buttom gray line detected");
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
