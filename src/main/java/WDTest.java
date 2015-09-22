import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;


public class WDTest {
    public static void main(String[] args) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        URL url = new URL("http://localhost:4321/");
        WebDriver driver = new RemoteWebDriver(url, capabilities);
        driver.get("http://vk.com/");

        File file = (File) getScreenshotAs(OutputType.FILE, driver);
        file.renameTo(new File("hello.png"));
        System.out.print("Hello");
    }

    public static <T> Object getScreenshotAs(OutputType<T> outputType, WebDriver driver) {
        Augmenter augmenter = new Augmenter();
        TakesScreenshot ts = (TakesScreenshot) augmenter.augment(driver);
        return ts.getScreenshotAs(outputType);
    }
}
