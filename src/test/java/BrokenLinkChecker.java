import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.Ref;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrokenLinkChecker {

    public static void main(String[] args) throws InterruptedException {
        //SETUP BEFORE TEST------------------------------------------
        WebDriver driver = new ChromeDriver();  //Launch chrome browser
        driver.manage().window().maximize();  //Maximize window
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); //Implicit wait of 10s

        //TEST STARTS FROM HERE------------------------------------------

        //Navigate to url
        driver.get("https://www.amazon.in/");
        Thread.sleep(5000);

        //Extract all link webelement from webpage
        List<WebElement> Extracted_Links = driver.findElements(By.xpath("//a[@href]"));

        //Refine the extracted links to remove duplicates and invalid urls
        Set<String> Unique_Links = new HashSet<>();

        for(WebElement link:Extracted_Links){
            String url = link.getAttribute("href");

            if(url == null || url.isEmpty())
                continue;

            if(url.startsWith("javascript") || url.startsWith("mailto") || url.startsWith("tel") || url.startsWith("#"))
                continue;

            Unique_Links.add(url);
        }



    }

}
