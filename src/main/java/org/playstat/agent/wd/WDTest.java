package org.playstat.agent.wd;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WDTest {
    private int ss = 0;

    public WDTest() {
    }

    public static void main(String[] args) throws MalformedURLException {
        WDTest app = new WDTest();
        app.run();
    }

    private void run() throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        URL url = new URL("http://localhost:4321/");
        WebDriver driver = new RemoteWebDriver(url, capabilities);
        driver.get("http://vk.com/");
        makeScreenShot(driver);
    }

    private void makeScreenShot(WebDriver driver) {
        File file = (File) getScreenshotAs(OutputType.FILE, driver);
        file.renameTo(new File(String.format("hello-%s.png", ss++)));
    }

    public static <T> Object getScreenshotAs(OutputType<T> outputType, WebDriver driver) {
        Augmenter augmenter = new Augmenter();
        TakesScreenshot ts = (TakesScreenshot) augmenter.augment(driver);
        return ts.getScreenshotAs(outputType);
    }
}
