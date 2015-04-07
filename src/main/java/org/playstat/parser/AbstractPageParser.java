package org.playstat.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPageParser implements IPageParser {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean storeErrorPages = false;
    private static final String ERRORS_FOLDER = "errors";
    private boolean complete;

    @Override
    public void setDataInputStream(InputStream in) throws ParserException {
        parse(in);
    }

    public void parse(InputStream in) throws ParserException {
        try {
            Document doc = Jsoup.parse(in, getCharsetName(), getBaseUrl());
            try {
                parse(doc);
                complete = true;
            } catch (ParserException e) {
                if (storeErrorPages) {
                    storeError(doc, e);
                }
                throw e;
            } catch (Exception e) {
                storeError(doc, e);
                throw new ParserException(e);
            }
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    protected String getCharsetName() {
        return "UTF-8";
    }

    public abstract void parse(Document doc) throws Exception;

    public void storeError(Document doc, Exception e)
            throws FileNotFoundException, IOException,
            UnsupportedEncodingException {
        File folder = new File(ERRORS_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String name = "error-" + UUID.randomUUID();
        FileOutputStream out = new FileOutputStream(new File(folder, name
                + ".html"));
        out.write(doc.html().getBytes("UTF-8"));
        out.close();
        out = new FileOutputStream(new File(folder, name + ".trace"));
        e.printStackTrace(new PrintStream(out));
        out.close();
        logger.error("error: " + e.getMessage() + " stored to " + name
                + ".trece");
    }

    public boolean isStoreErrorPages() {
        return storeErrorPages;
    }

    public void setStoreErrorPages(boolean storeErrorPages) {
        this.storeErrorPages = storeErrorPages;
    }

    public boolean isComplete() {
        return complete;
    }
    public abstract String getBaseUrl();
}
