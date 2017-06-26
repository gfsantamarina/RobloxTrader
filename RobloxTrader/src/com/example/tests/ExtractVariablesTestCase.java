package com.example.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import roblox.model.Order;
import roblox.model.Position;
import roblox.model.RobloxTradeState;

public class ExtractVariablesTestCase {
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
		loginUser();
	}

	public void loginUser() throws Exception {
		driver.findElement(By.id("txtUsername")).click();
		driver.findElement(By.id("txtUsername")).clear();
		driver.findElement(By.id("txtUsername")).sendKeys("XXX");
		driver.findElement(By.id("txtPassword")).clear();
		driver.findElement(By.id("txtPassword")).sendKeys("XXX");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.linkText("Trade")).click();
		driver.findElement(By.linkText("Trade Currency")).click();
	}

	private List<Order> extractOrders(String orders) throws ParseException {
		List<Order> list = Collections.synchronizedList(new LinkedList<Order>());

		String ordersArray[] = orders.split("[\n]");
		for (String aString : ordersArray) {
			if (aString.contains("@"))
				list.add(Order.parseOrder(aString));
		}

		return list;
	}

	private List<Position> extractPositions(String positions) throws ParseException {
		List<Position> list = Collections.synchronizedList(new LinkedList<Position>());

		String positionsArray[] = positions.split("[\n]");
		for (String aString : positionsArray) {
			if (aString.contains("@"))
				list.add(Position.parsePosition(aString));
		}

		return list;
	}

	@Test
	public void testExtractVariablesTestCase() throws Exception {

		final DecimalFormat balanceFormat = new DecimalFormat("#,###");

		// Grab the data
		Date now = new Date();
		String TicketOrders = driver.findElement(By.cssSelector("div.CurrencyBids")).getText();
		String ROBUXOrders = driver.findElement(By.cssSelector("div.CurrencyOffers")).getText();
		String ROBUXBalance = driver.findElement(By.cssSelector("div#RobuxAlertCaption")).getText();
		String TicketsBalance = driver
				.findElement(By.cssSelector(
						"div#ctl00_ctl00_cphBanner_MenuRedesign_BannerAlertsAndOptionsLoginView_BannerAlertsAndOptions_Authenticated_TicketsAlertCaption"))
				.getText();
		String OpenROBUXPositions = driver.findElement(By.cssSelector("table.OpenOffersContent")).getText();
		// Click on the "My Open Tickets Positions" heading
		driver.findElement(By.xpath("//div[@id='accordion']/h4[2]")).click();
		String OpenTicketsPositions = driver.findElement(By.cssSelector("table.OpenBidsContent")).getText();

		RobloxTradeState aRobloxTradeState = new RobloxTradeState(now, (Long) balanceFormat.parse(ROBUXBalance),
				(Long) balanceFormat.parse(TicketsBalance), 0, extractOrders(ROBUXOrders), extractOrders(TicketOrders),
				extractPositions(OpenROBUXPositions), extractPositions(OpenTicketsPositions));

		System.out.println(aRobloxTradeState);
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
