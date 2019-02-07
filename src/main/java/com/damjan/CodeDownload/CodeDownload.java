
package com.damjan.CodeDownload;

import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.FileUtils.copyURLToFile;


public class CodeDownload {
    static protected String course = "";
    static protected String DRIVER_FILE;

    static protected void writeToFile(String name, String contents) throws Exception
    {
        BufferedWriter writer= new BufferedWriter(new FileWriter(name));
        writer.write(contents);
        writer.close();
    }

    static protected void extractDriver() throws Exception
    {
        ClassLoader classLoader = CodeDownload.class.getClassLoader();
        String driverFile;

        if(System.getProperty("os.name").startsWith("Windows"))
            driverFile = "geckodriver.exe";
        else if(System.getProperty("os.name").startsWith("Mac"))
            driverFile = "geckodriver";
        else throw new Exception("System not supported");


        //URL resource = classLoader.getResource("resources" + File.separator + driverFile);
        File driver = new File(driverFile);

        /*if (!driver.exists()) {
            driver.createNewFile();
            copyURLToFile(resource, driver);
        }*/
        
        driver.setExecutable(true);
        System.setProperty("webdriver.gecko.driver", driver.getAbsolutePath());
    }

    static public void main(String args[]) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        extractDriver();


        System.out.println("Enter course URL: ");
        course = br.readLine();

        WebDriver driver = new FirefoxDriver();
        try {
            driver.get(course);

            System.out.println("Login in the browser!");

            (new WebDriverWait(driver, 100)).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return d.getTitle().startsWith("Home");
                }
            });

            driver.get(course);
            List<WebElement> exercises = driver.findElements(By.className("label-success"));
            List<String> urlsOfExercises = new ArrayList<String>();
            for (WebElement success : exercises) {
                WebElement followingSibling = success.findElement(By.xpath("following-sibling::*[1]"));
                urlsOfExercises.add(followingSibling.getAttribute("href") + "?programmingLanguageId=3");
            }

            String courseName = driver.findElement(By.tagName("small")).getText();
            System.out.println(courseName);


            for (String url : urlsOfExercises) {
                driver.get(url);

                String problemName = driver.findElement(By.id("problem-name")).getAttribute("innerHTML").split("<small>")[0].trim();
                String problemCode = driver.findElement(By.id("code")).getAttribute("innerHTML");


                problemCode = StringEscapeUtils.unescapeHtml4(problemCode);
                String problemText = driver.findElement(By.id("problem-text")).getAttribute("innerHTML");
                problemText = StringEscapeUtils.unescapeHtml4(problemText);

                File newFolder = new File(courseName + File.separator + problemName);
                boolean created =  newFolder.mkdirs();

                writeToFile(newFolder.getAbsolutePath() + File.separator + "ProblemText.htm", problemText);
                writeToFile(newFolder.getAbsolutePath() + File.separator + "Code.java", problemCode);
                System.out.println("Downloaded " + problemName);
            }
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        driver.quit();
        System.out.println("Finished downloading!");

    }
}
