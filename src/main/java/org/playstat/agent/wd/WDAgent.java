package org.playstat.agent.wd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.playstat.agent.IAgent;
import org.playstat.spider.HTTPRequest;
import org.playstat.spider.HTTPResponse;
import org.playstat.spider.Transaction;

public class WDAgent implements IAgent {
    private WebDriver driver;

    public WDAgent(String wdServerUrl) throws IOException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        URL url = new URL(wdServerUrl);
        driver = new RemoteWebDriver(url, capabilities);
    }

    public InputStream go(Transaction t) throws IOException {
        driver.get(t.getUrl());
        // TODO use page encoding
        ByteArrayInputStream in = new ByteArrayInputStream(driver.getPageSource().getBytes("UTF-8"));
        return in;
    }

    @Override
    public void setProxy(Proxy proxy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addCookie(String host, String name, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCookie(String host, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream post(Transaction t) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCharset(String charset) {
        // TODO Auto-generated method stub

    }

    @Override
    public HTTPResponse send(HTTPRequest request) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream post(HTTPRequest t) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
