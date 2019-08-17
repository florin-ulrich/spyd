import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ytmp3ccConverter implements YTMP3Converter {

    @Override
    public void downloadLinksToMp3(List<String> links, String downloadPath) {
        WebDriver driver = getDefaultDriver(downloadPath);
        for (String link :
                links) {
            downloadFromLink(link, driver);
        }
        //co.addArguments("headless");
    }

    public static void main(String[] args) {
        List<String> links = new ArrayList<>();
        links.add("https://www.youtube.com/watch?v=auzfTPp4moA");
        links.add("https://www.youtube.com/watch?v=i3Jv9fNPjgk");
        links.add("https://www.youtube.com/watch?v=ISy0Hl0SBfg");
        ytmp3ccConverter y = new ytmp3ccConverter();
        y.downloadLinksToMp3(links, "D:\\temporary_quote_on_quote\\selenium_dl");
    }

    private void downloadFromLink(String link, WebDriver driver) {
        driver.get("https://ytmp3.cc/");
        WebElement element = driver.findElement(By.name("video"));
        element.sendKeys(link);
        element.submit();
        WebElement downloadLink = (new WebDriverWait(driver, 120))
                .until(ExpectedConditions.presenceOfElementLocated(By.linkText("Download")));
        downloadLink.click();
    }

    private static WebElement waitForElement(By by, WebDriver driver) {
        while (true) {
            try {
                return driver.findElement(by);
            } catch (Exception e) {
                waitAround(2000);
            }
        }
    }

    private static void waitAround(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // le lenny face
        }
    }

    private WebDriver getDefaultDriver(String downloadPath) {
        System.setProperty("webdriver.chrome.driver", "D:\\temporary_quote_on_quote\\chromedriver.exe");
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadPath);
        ChromeOptions co = new ChromeOptions();
        co.addExtensions(new File("C:\\Users\\flori\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Extensions\\cfhdojbkjhnklbpkdaibdccddilifddb\\3.6_0.crx"));
        co.setExperimentalOption("prefs", chromePrefs);
        return new ChromeDriver(co);
    }
}
