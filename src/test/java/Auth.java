import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.WebDriver;

public class Auth {

    Logger logger = LogManager.getLogger(Auth.class);
    protected static WebDriver driver;
    protected Actions action;

    @Before
    public void startUp(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        action = new Actions(driver);
        logger.info("Драйвер запущен.");
    }
    @After
    public void end(){
        if (driver!=null) {
            File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            saveFile(file);
            driver.quit();
            logger.info("Драйвер остановлен. Сделан скриншот.");
        }
    }

    private void saveFile(File data) {
        String fileName = "target/" + System.currentTimeMillis() + ".png";
        try {
            FileUtils.copyFile(data, new File(fileName));
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @Test
    public void positiveAuthByCredentials() throws Exception {
        String email = getProperties("email1");
        String password = decrypt(getProperties("password1"));
        authWithCredentials(email, password);
        String actualEmail = checkEmailAfterAuth(email);
        Assert.assertEquals("Profile Error. Email value is not the same.", email, actualEmail);
    }

    @Test
    public void positiveAuthByCredentialsWithLogout() throws Exception {
        String email = getProperties("email2");
        String password = decrypt(getProperties("password2"));
        authWithCredentials(email, password);
        String actualEmail = checkEmailAfterAuth(email);
        Assert.assertEquals("Profile Error. Email value is not the same.", email, actualEmail);

        getElement(By.xpath("//*[contains(text(),'Log out')]")).click();
        email = getProperties("email1");
        password = decrypt(getProperties("password1"));
        authWithCredentials(email, password);
        actualEmail = checkEmailAfterAuth(email);
        Assert.assertEquals("Profile Error. Email value is not the same.", email, actualEmail);
    }

    @Test
    public void negativeAuthByCredentials() throws Exception {
        String email = getProperties("email1");
        String password = "randomWrongPassword";
        authWithCredentials(email, password);
        String loginAlert = "//*[@class='signup__error-item']";
        String actualAlert = getElement(By.xpath(loginAlert)).getText();
        String expectedAlert = "The email or password you entered is incorrect.\n" +
                "Please try again.";
        Assert.assertEquals("Incorrect credentials. AlertMessage != " + expectedAlert, expectedAlert, actualAlert);
    }

    @Test
    public void negativeAuthByCredentialsWithEmptyPassword() throws Exception {
        String email = getProperties("email1");
        String password = "";
        authWithCredentials(email, password);
        String loginAlert = "//*[@class='signup__error-item']";
        String actualAlert = getElement(By.xpath(loginAlert)).getText();
        String expectedAlert = "Please enter your password.";
        Assert.assertEquals("Incorrect credentials. AlertMessage != " + expectedAlert, expectedAlert, actualAlert);
    }

    @Test
    public void negativeAuthByEmptyCredentials() throws Exception {
        String email = "";
        String password = "";
        authWithCredentials(email, password);
        String actualLoginAlert = getElement(By.xpath("(//*[@class='signup__error-item'])[1]")).getText();
        String expectedLoginAlert = "Please enter your email address.";

        String actualPasswordAlert = getElement(By.xpath("(//*[@class='signup__error-item'])[2]")).getText();
        String expectedPasswordAlert = "Please enter your password.";
        Assert.assertEquals("Incorrect credentials. AlertMessage != " + expectedLoginAlert, expectedLoginAlert, actualLoginAlert);
        Assert.assertEquals("Incorrect credentials. AlertMessage != " + expectedPasswordAlert, expectedPasswordAlert, actualPasswordAlert);
    }

    @Test
    public void checkForgotPassword() throws Exception {
        String email = "";
        String password = "";
        authWithCredentials(email, password);
        String actualLink = getElement(By.xpath("//*[contains(text(),'Forgot password?')]")).getAttribute("href");
        String expectedLink = "https://miro.com/recover/";
        Assert.assertEquals("Incorrect credentials. ForgotPasswordLink != " + expectedLink, expectedLink, actualLink);
    }


    public WebElement getElement(By locator){
        logger.info("Получение элемента {}", locator);
        return new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public String getProperties(String name) throws Exception{
        String configfile = "src/main/resources/config.properties";
        FileInputStream propFile = new FileInputStream(configfile);
        Properties p = new Properties(System.getProperties());
        p.load(propFile);

        // set the system properties
        System.setProperties(p);
        logger.info("Получение переменной " + name + " из файла " + configfile);
        return System.getProperties().getProperty(name);
    }

    public String decrypt(String hash) throws Exception {
        EncryptDecrypt td = new EncryptDecrypt();
        String decrypted = td.decrypt(hash);
        logger.info("decrypting password");
        return decrypted;
    }

    public void authWithCredentials(String email, String password) throws Exception {
        driver.get(getProperties("miroLoginPage"));
        getElement(By.xpath("//input[@data-autotest-id='mr-form-login-email-1']")).sendKeys(Keys.chord((Keys.CONTROL), "a"), email);
        getElement(By.xpath("//input[@data-autotest-id='mr-form-login-password-1']")).sendKeys(Keys.chord((Keys.CONTROL), "a"), password);
        getElement(By.xpath("//button[@data-autotest-id='mr-form-login-btn-signin-1']")).click();
    }

    public String checkEmailAfterAuth(String email) throws Exception {
        driver.get(getProperties("dashboard"));
        getElement(By.xpath("//*[@class='user-profile__button']/div/img")).click();
        String actualEmail = getElement(By.xpath("//*[@class='user-profile__email']")).getText();
        return actualEmail;
    }

}
