import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Ref;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        //Extract all link web element from webpage
        List<WebElement> Extracted_Links = driver.findElements(By.xpath("//a[@href]"));

        // Call the refine function to filter duplicates and invalid links
        Set<String> Refined_Links = refineLinks(Extracted_Links);
        System.out.println("TOTAL LINKS FOUND- "+Refined_Links.size());

        checkLinks(Refined_Links);
    }

    public static Set<String> refineLinks(List<WebElement> Extracted_Links){

        /**
         * This function refines links by removing duplicates and invalid links
         * like JavaScript(0), #, mailto etc.
         *
         * @param extractedLinks List of WebElements containing raw anchor tags
         * @return Set of unique, valid URL strings
         */

        //Define a set to store all filtered links. Additionally, it won't allow duplicate entries
        Set<String> Unique_Links = new HashSet<>();

        for(WebElement link:Extracted_Links){
            String url = link.getAttribute("href");

            if(url == null || url.isEmpty())
                continue;

            if(url.startsWith("javascript") || url.startsWith("mailto") || url.startsWith("tel") || url.startsWith("#"))
                continue;

            Unique_Links.add(url);
        }

        return  Unique_Links;
    }

    public static void checkLinks(Set<String> Refined_Links) throws InterruptedException {
        /**
         * This function checks if a URL is alive by sending a HEAD request.
         *
         * @param linkUrl the URL to check
         * @return "VALID - <code>" if reachable, "BROKEN - <reason>" otherwise
         */

        //Create a thread pool of 10 threads to check links in parallel
        ExecutorService executor = Executors.newFixedThreadPool(10);

        //To store list of broken and valid links after test
        List<String> Broken_Links = new ArrayList<>();
        List <String> Valid_Links = new ArrayList<>();

        System.out.println("TESTING ALL LINKS. THIS MAY TAKE A WHILE...");

        executor.submit(()-> {
                    try {
                        for (String link : Refined_Links){
                            URL url = new URL(link);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("HEAD");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            connection.setInstanceFollowRedirects(true);

                            // Mimic a real browser to avoid 403s on some servers
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

                            int responseCode = connection.getResponseCode();

                            if(responseCode >= 400)
                                Broken_Links.add("LINK: " + link + " , RESPONSE: " + responseCode);
                            else
                                Valid_Links.add("LINK: " + link + " , RESPONSE: " + responseCode);

                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        //LOG OUTPUT IN CONSOLE---------------------------------------------------------

        //Print valid and broken link count
        System.out.println("VALID LINKS FOUND: "+ Valid_Links.size());
        System.out.println("TOTAL BROKEN LINKS: "+ Valid_Links.size());

        //Print list of valid and broken links
        System.out.println("BROKEN LINKS:");
        for (String link : Broken_Links) {
            System.out.println(link);
        }
        System.out.println("VALID LINKS:");
        for (String link : Valid_Links) {
            System.out.println(link);
        }

    }

}
