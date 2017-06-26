package com.example.tests;

import java.io.File;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.Select;

public class OpenBidsContentTestCase {
	private WebDriver driver;
	private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver(new FirefoxProfile(
				new File("C:\\Users\\George\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\s3wizz59.default")));
		baseUrl = "http://www.roblox.com/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get(baseUrl);
		testLogin();
	}

	public void testLogin() throws Exception {
		driver.findElement(By.id("txtUsername")).click();
		driver.findElement(By.id("txtUsername")).clear();
		driver.findElement(By.id("txtUsername")).sendKeys("jayhawk8d");
		driver.findElement(By.id("txtPassword")).clear();
		driver.findElement(By.id("txtPassword")).sendKeys("r0bl0x");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.linkText("Trade")).click();
		driver.findElement(By.linkText("Trade Currency")).click();
	}

	@Test
	public void testOpenBidsContent() throws Exception {
		driver.findElement(By.xpath("//div[@id='accordion']/h4[2]")).click();
		String OpenBidsContent = driver.findElement(By.cssSelector("table.OpenBidsContent")).getText();
		System.out.println("OpenBidsContent is " + OpenBidsContent);
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
}
