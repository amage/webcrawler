package org.playstat.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.playstat.agent.IAgent;
import org.playstat.agent.ICaptchaSolver;
import org.playstat.agent.RequestMethod;
import org.playstat.agent.Transaction;
import org.playstat.agent.nullagent.NullAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ICache cache = new FileCache();
    private final IAgent agent;
    private boolean useReferer = true;
    private int historySize = 512;
    private final LinkedList<Transaction> history = new LinkedList<>();

    private String charsetName = "UTF-8";
    private String baseUrl = "";
    private ICaptchaSolver captchaSolver;
    private boolean cacheEnable = true;

    public WebClient() {
        this.agent = new NullAgent();
    }

    public WebClient(IAgent agent) {
        this.agent = agent;
    }

    public Document go(String url) throws IOException {
        return go(url, baseUrl);
    }

    public InputStream post(String url, Map<String, String> params)
            throws IOException {
        return agent.post(Transaction.create(url, RequestMethod.POST, params, ""));
    }

    public InputStream goRaw(String url) throws IOException {
        if (cacheEnable) {
            File pageFile = cache.getCacheFile(url);
            try {
                if (pageFile.exists()) {
                    return new FileInputStream(pageFile);
                }
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            Files.copy(request(url), pageFile.toPath());
            return new FileInputStream(pageFile);
        } else {
            return request(url);
        }
    }

    public Document go(String url, String baseUrl) throws IOException {
        final Transaction t = Transaction.create(url);

        this.setBaseUrl(baseUrl);
        final File pageFile = cache.getCacheFile(t.getUrl());
        if (cacheEnable) {
            if (pageFile.exists()) {
                try {
                    return Jsoup.parse(pageFile, charsetName, baseUrl);
                } catch (FileNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        final Document result = Jsoup.parse(request(url), getCharsetName(), baseUrl);

        if (getCaptchaSolver() != null) {
            if (getCaptchaSolver().isCaptchaPage(result)) {
                getCaptchaSolver().solve(this, result);
                return go(t.getUrl(), baseUrl);
            }
        }
        if (cacheEnable) {
            FileOutputStream out = new FileOutputStream(pageFile);
            out.write(result.html().getBytes(charsetName));
            out.close();
        }
        return result;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        agent.setCharset(charsetName);
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

    // TODO: version without outFileName
    public void download(Transaction t, String outFileName) throws IOException {
        final File outFile = new File(outFileName);
        outFile.getParentFile().mkdirs();

        logger.trace("downloading file: " + t.getUrl());
        ReadableByteChannel rbc = Channels.newChannel(getWebAgent().go(t));
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    public IAgent getWebAgent() {
        return agent;
    }

    public ICaptchaSolver getCaptchaSolver() {
        return captchaSolver;
    }

    public void setCaptchaSolver(ICaptchaSolver captchaSolver) {
        this.captchaSolver = captchaSolver;
    }

    private InputStream request(String url) throws IOException {
        Transaction t = Transaction.create(url);
        if (!history.isEmpty() && useReferer) {
            t.addRequestParam("Referer", history.getFirst().getUrl());
        }
        putToHistory(t);
        return agent.go(t);
    }

    private void putToHistory(Transaction transaction) {
        if (history.size() > historySize) {
            history.removeLast();
        }
        history.addFirst(transaction);
    }
}
