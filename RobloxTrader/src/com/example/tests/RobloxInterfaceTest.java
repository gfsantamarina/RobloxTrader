package com.example.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import roblox.driver.RobloxInterface;
import roblox.model.BUXTIXTradeOrder;
import roblox.model.RobloxTradeState;
import roblox.model.RobloxTradeState.Currency;

public class RobloxInterfaceTest {

	RobloxInterface aRobloxInterface;
	RobloxTradeState aTradeState;

	@Before
	public void setUp() throws Exception {
		String FirefoxProfilePath = "C:\\Users\\George\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\s3wizz59.default";
		aRobloxInterface = new RobloxInterface(FirefoxProfilePath);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTakeAction() throws Exception {
		aTradeState = aRobloxInterface.getRobloxTradeState();
		System.out.println(aTradeState);
		aTradeState.setBuyOrder(new BUXTIXTradeOrder(Currency.ROBUX, 100, 1200));
		if (aTradeState.isActionNeeded()) {
			aRobloxInterface.takeAction(aTradeState);
		}
	}

}
