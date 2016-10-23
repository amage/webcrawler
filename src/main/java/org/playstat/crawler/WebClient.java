package org.playstat.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
        return agent.go(Transaction.create(url, RequestMethod.POST, params, "")).getBody();
    }

    public Document go(Transaction t, String baseUrl) throws IOException {
        this.setBaseUrl(baseUrl);
        if(t.isComplete()) {
            return Jsoup.parse(t.getResponse().getBody(), getCharsetName(), getBaseUrl());
        }

        File pageFile = null;
        if (cacheEnable) {
            pageFile = cache.getCacheFile(t.getRequest());
            if (pageFile.exists()) {
                try {
                    return Jsoup.parse(pageFile, charsetName, baseUrl);
                } catch (FileNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        final Document result = Jsoup.parse(request(t.getUrl()), getCharsetName(), baseUrl);

        // TODO: move outside
        if (getCaptchaSolver() != null) {
            if (getCaptchaSolver().isCaptchaPage(result)) {
                getCaptchaSolver().solve(this, result);
                return go(t.getUrl(), baseUrl);
            }
        }
        if (cacheEnable) {
            try(FileOutputStream out = new FileOutputStream(pageFile)){
                out.write(result.html().getBytes(charsetName));
            }
        }
        return result;

    }
    public Document go(String url, String baseUrl) throws IOException {
        return go(Transaction.create(url), baseUrl);
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
        ReadableByteChannel rbc = Channels.newChannel(getWebAgent().go(t).getBody());
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
        return agent.go(t).getBody();
    }

    private void putToHistory(Transaction transaction) {
        if (history.size() > historySize) {
            history.removeLast();
        }
        history.addFirst(transaction);
    }
}
