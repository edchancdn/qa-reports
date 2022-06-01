package org.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ZoomTest {
    WebDriver driver;
    WebDriverWait wait;
    ExtentSparkReporter htmlReporter;
    ExtentReports extent;
    ExtentTest logger;

    @BeforeSuite
    public void setUp() {
        htmlReporter = new ExtentSparkReporter("extentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);

        ChromeOptions options = new ChromeOptions();
        options.setBinary(/usr/bin/google-chrome);
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(4));
    }

    @AfterMethod
    public void getResult(ITestResult result) throws Exception {
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " Test case FAILED due to issues below:", ExtentColor.RED));
            logger.fail(result.getThrowable());

            File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String sPath = result.getName() + "_failure_" + dtf.format(now) + ".png";

            FileUtils.copyFile(file, new File(sPath));
            logger.fail("Attached screenshot").addScreenCaptureFromPath(sPath);
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.log(Status.PASS, MarkupHelper.createLabel(result.getName() + " Test case PASSED", ExtentColor.GREEN));
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " Test case SKIPPED", ExtentColor.BLUE));
        }

        logger.info("Test case " + result.getName() + " completed");
    }

    private Boolean webElementExists(By webElementSelector) {
        return driver.findElements(webElementSelector).size() > 0;
    }

    @Test
    public void tc1() {
        logger = extent.createTest("Zoom Join Test", "Test to validate join button");
        logger.log(Status.INFO, "Starting test case tc1");

        driver.get("https://zoom.us/");
        logger.pass("Opened zoom site");
        driver.manage().window().maximize();
        logger.pass("Browser maximized");

        Assert.assertEquals(driver.getTitle(), "Video Conferencing, Cloud Phone, Webinars, Chat, Virtual Events | Zoom");
        logger.pass("Zoom site loaded");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#btnJoinMeeting"))).click();
        logger.pass("Click on Join link");

        Assert.assertEquals(driver.getTitle(), "Join Meeting - Zoom");
        logger.pass("Join page loaded");

        WebElement btnJoin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#btnSubmit")));
        Assert.assertEquals(btnJoin.getAttribute("disabled"), "true");
        logger.pass("Join button is disabled");

        driver.findElement(By.cssSelector("#join-confno")).sendKeys("1234567890");
        logger.pass("Enter meeting ID in input box");

        Assert.assertNull(btnJoin.getAttribute("disabled"));
        logger.pass("Join button is enabled");
    }

    @Test
    public void tc2() {
        logger = extent.createTest("Zoom Contact Sales Test", "Test to validate contact sales fields");
        logger.log(Status.INFO, "Starting test case tc2");

        driver.get("https://zoom.us/");
        logger.pass("Opened zoom site");
        driver.manage().window().maximize();
        logger.pass("Browser maximized");

        Assert.assertEquals(driver.getTitle(), "Video Conferencing, Cloud Phone, Webinars, Chat, Virtual Events | Zoom");
        logger.pass("Zoom site loaded");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.top-contactsales.top-sales[href='https://explore.zoom.us/contactsales']"))).click();
        logger.pass("Click on Contact Sales link");

        Assert.assertEquals(driver.getTitle(), "Contact Sales | Zoom");
        logger.pass("Contact Sales page loaded");

        // Intentionally set email value here to an invalid format to showcase screen capture attachment in report.
        // To pass this step, change the sendKeys value to johndoe@mail.com
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#email"))).sendKeys("johndoe@mail.com", Keys.TAB);
        Assert.assertFalse(webElementExists(By.cssSelector("span[for='email'][class='has-error help-block']")), "Invalid email address format entered. \n ");
        logger.pass("Enter valid work email address in input box");

        driver.findElement(By.cssSelector("#company")).sendKeys("Acme Inc");
        logger.pass("Enter company name in input box");

        driver.findElement(By.cssSelector("#first_name")).sendKeys("John");
        logger.pass("Enter first name in input box");

        driver.findElement(By.cssSelector("#last_name")).sendKeys("Doe");
        logger.pass("Enter last name in input box");

        Select drpEmployeeCnt = new Select(driver.findElement(By.cssSelector("#employee_count")));
        drpEmployeeCnt.selectByVisibleText("51-250");
        logger.pass("Select 51-250 employee count from drop-down list");
    }

    @AfterSuite
    public void tearDown() {
        logger.info("Test suite completed");
        driver.quit();
        extent.flush();
    }
}
