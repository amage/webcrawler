package org.playstat.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
    private final ICache cache;
    private final IAgent agent;
    private ICaptchaSolver captchaSolver;
    private final WebClientSettings settings = WebClientSettings.defs();
    private long delay = 0L;
    private long lastRequest = 0L;

    public WebClient() {
        this(new FileCache());
    }

    public WebClient(IAgent agent) {
        this.cache = new FileCache();
        this.agent = agent;
    }

    public WebClient(FileCache cache) {
        this.agent = new NullAgent(settings);
        this.cache = cache;
    }

    public Document go(String url) throws IOException {
        return go(url, settings.getBaseUrl());
    }

    public InputStream post(String url, Map<String, String> params) throws IOException {
        return agent.go(Transaction.create(url, RequestMethod.POST, params, "")).getBody();
    }

    public Document go(String url, String baseUrl) throws IOException {
        long timeDelta = System.currentTimeMillis() - lastRequest;
        if (timeDelta < getDelay()) {
            try {
                Thread.sleep(getDelay() - timeDelta);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        this.setBaseUrl(baseUrl);

        final Transaction t = Transaction.create(url);
        if (settings.isCacheEnable() && cache.isCahed(t.getRequest())) {
            return Jsoup.parse(cache.getCacheFile(t.getRequest()), settings.getCharsetName(), baseUrl);
        }

        final File pageFile = getCache().getCacheFile(t.getRequest());

        final Document result = Jsoup.parse(agent.go(t).getBody(), settings.getCharsetName(), baseUrl);
        lastRequest = System.currentTimeMillis();

        if (getCaptchaSolver() != null) {
            if (getCaptchaSolver().isCaptchaPage(result)) {
                getCaptchaSolver().solve(this, result);
                return go(t.getUrl(), baseUrl);
            }
        }
        if (settings.isCacheEnable()) {
            cache.cache(t);
            final FileOutputStream out = new FileOutputStream(pageFile);
            out.write(result.html().getBytes(settings.getCharsetName()));
            out.close();

        }
        return result;
    }

    public WebClientSettings getSettings() {
        return settings;
    }

    public void setCharsetName(String charsetName) {
        this.settings.setCharsetName(charsetName);
        agent.setCharset(charsetName);
    }

    public void setBaseUrl(String baseUrl) {
        this.settings.setBaseUrl(baseUrl);
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.settings.setCacheEnable(cacheEnable);
    }

    // TODO: version without outFileName
    public void download(Transaction t, String outFileName) throws IOException {
        final File outFile = new File(outFileName);
        outFile.getParentFile().mkdirs();

        logger.trace("downloading file: " + t.getUrl());
        final ReadableByteChannel rbc = Channels.newChannel(getWebAgent().go(t).getBody());
        final FileOutputStream fos = new FileOutputStream(outFile);
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

    public ICache getCache() {
        return cache;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public Transaction getLastTransaction() {
        if (agent.getHistory().isEmpty()) {
            return null;
        }
        return agent.getHistory().getFirst();
    }
}
