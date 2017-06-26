package roblox.driver;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import roblox.model.BUXTIXTradeOrder;
import roblox.model.Order;
import roblox.model.Position;
import roblox.model.RobloxTradeState;
import roblox.model.RobloxTradeState.Currency;

public class RobloxInterface {

	private static ChromeDriverService service;
	private static final DecimalFormat balanceFormat = new DecimalFormat("#,###");
	private WebDriver driver;
	private String baseUrl = "http://www.roblox.com/";
	private StringBuffer verificationErrors = new StringBuffer();
	private String BrowserProfilePath;

	private boolean isLoggedIn;

	public RobloxInterface(String BrowserProfilePath) {
		this.BrowserProfilePath = BrowserProfilePath;
	}

	public void initializeAndLogin() throws Exception {
		service = ChromeDriverService.createDefaultService();
		service.start();
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments(Arrays.asList("--user-data-dir=" + BrowserProfilePath));
		driver = new ChromeDriver(service, chromeOptions);
		setDriverTimeout(10);
		driver.get(baseUrl);
		loginUser();
	}

	private void setDriverTimeout(long timeout) {
		driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
	}

	private void loginUser() throws Exception {
		driver.findElement(By.id("txtUsername")).click();
		driver.findElement(By.id("txtUsername")).clear();
		driver.findElement(By.id("txtUsername")).sendKeys("jayhawk8d");
		driver.findElement(By.id("txtPassword")).clear();
		driver.findElement(By.id("txtPassword")).sendKeys("r0bl0x");
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.linkText("Trade")).click();
		driver.findElement(By.linkText("Trade Currency")).click();
		isLoggedIn = true;
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

	private void scrollToTop() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
	}

	public RobloxTradeState getRobloxTradeState() throws Exception {

		if (!isLoggedIn) {
			initializeAndLogin();
		} else {
			// If logged in, just refresh the trade section
			scrollToTop();
			driver.findElement(By.xpath("//div[@id='TradeCurrencyContainer']//a[@href='Money.aspx?tab=TradeCurrency']"))
					.click();
		}

		// Grab the data
		Date now = new Date();
		String TicketOrders = driver.findElement(By.cssSelector("div.CurrencyBids")).getText();
		String ROBUXOrders = driver.findElement(By.cssSelector("div.CurrencyOffers")).getText();
		String ROBUXBalance = driver.findElement(By.cssSelector("div#RobuxAlertCaption")).getText();
		String Spread = driver.findElement(By.xpath("//div[@class='TableRow']//div[@class='Spread']")).getText();
		String TicketsBalance = driver
				.findElement(By.cssSelector(
						"div#ctl00_ctl00_cphBanner_MenuRedesign_BannerAlertsAndOptionsLoginView_BannerAlertsAndOptions_Authenticated_TicketsAlertCaption"))
				.getText();
		// Check if over 10K
		if (TicketsBalance.contains("K+")) {
			String TicketsBalanceOver10k = driver
					.findElement(By
							.xpath("//*[@id='ctl00_ctl00_cphBanner_MenuRedesign_BannerAlertsAndOptionsLoginView_BannerAlertsAndOptions_Authenticated_TicketsWrapper']"))
					.getAttribute("original-title");
			// Get the xx,xxx part of "Tickets xx,xxx"
			TicketsBalance = (TicketsBalanceOver10k.split(" "))[1];
		}
		String OpenROBUXPositions = "";
		String OpenTicketsPositions = "";
		setDriverTimeout(1); // Fail right away if we can't find the open ROBUX
								// or tickets positions
		try {
			OpenROBUXPositions = driver.findElement(By.cssSelector("table.OpenOffersContent")).getText();
		} catch (NoSuchElementException ex) {
		}
		// Click on the "My Open Tickets Positions" heading in order to obtain
		// the
		// Open Tickets position. Otherwise, Selenium does not retrieve it.
		driver.findElement(By.xpath("//div[@id='accordion']/h4[2]")).click();
		// Wait 100ms while the accordion opens! Otherwise, it picks up the
		// header but not the contents of the table
		Thread.sleep(100);
		try {
			OpenTicketsPositions = driver.findElement(By.cssSelector("table.OpenBidsContent")).getText();
		} catch (NoSuchElementException ex) {
		}
		scrollToTop();
		setDriverTimeout(10); // Bump the timeout back to 10 secs

		return new RobloxTradeState(now, (Long) balanceFormat.parse(ROBUXBalance),
				(Long) balanceFormat.parse(TicketsBalance), Long.parseLong(Spread), extractOrders(ROBUXOrders),
				extractOrders(TicketOrders), extractPositions(OpenROBUXPositions),
				extractPositions(OpenTicketsPositions));
	}

	public void shutDown() throws Exception {
		scrollToTop();
		System.out.println("Logging out...");
		driver.findElement(By.linkText("Logout")).click();
		Thread.sleep(5000);
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			System.err.println(verificationErrorString);
		}
		service.stop();
	}

	private String getCurrencyNameMixedCase(Currency aCurrency) {
		return (aCurrency == Currency.ROBUX) ? "Robux" : "Tickets";
	}

	public void takeAction(RobloxTradeState aTradeState) {
		BUXTIXTradeOrder buyOrder = aTradeState.getBuyOrder();
		String sellCurrencyName = getCurrencyNameMixedCase(buyOrder.getSellCurrency());
		String buyCurrencyName = getCurrencyNameMixedCase(buyOrder.getBuyCurrency());
		if (buyOrder != null) {
			driver.findElement(By.xpath("//span[@class='LimitOrderRadioButton']//input")).click();
			driver.findElement(By.xpath("//input[@id='ctl00_ctl00_cphRoblox_cphMyRobloxContent_HaveAmountTextBox']"))
					.sendKeys(Long.toString(aTradeState.getBuyOrder().getSellAmount()));
			new Select(driver.findElement(
					By.xpath("//select[@id='ctl00_ctl00_cphRoblox_cphMyRobloxContent_HaveCurrencyDropDownList']")))
							.selectByVisibleText(sellCurrencyName);
			driver.findElement(By.xpath("//input[@id='ctl00_ctl00_cphRoblox_cphMyRobloxContent_WantAmountTextBox']"))
					.sendKeys(Long.toString(aTradeState.getBuyOrder().getBuyAmount()));
			new Select(driver.findElement(
					By.xpath("//select[@id='ctl00_ctl00_cphRoblox_cphMyRobloxContent_WantCurrencyDropDownList']")))
							.selectByVisibleText(buyCurrencyName);
			driver.findElement(By.id("ctl00_ctl00_cphRoblox_cphMyRobloxContent_SubmitTradeButton")).click();
			driver.switchTo().alert().accept();
		}

	}
}
