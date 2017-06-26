package roblox.model;

import roblox.model.RobloxTradeState.Currency;

public class BUXTIXTradeOrder {

	Currency sellCurrency;
	long sellAmount;
	long buyAmount;

	public BUXTIXTradeOrder(Currency sellCurrency, long sellAmount, long buyAmount) {
		super();
		this.sellCurrency = sellCurrency;
		this.sellAmount = sellAmount;
		this.buyAmount = buyAmount;
	}

	public Currency getBuyCurrency() {
		return (getSellCurrency() == Currency.ROBUX) ? Currency.TICKETS : Currency.ROBUX;
	}

	public Currency getSellCurrency() {
		return sellCurrency;
	}

	public long getSellAmount() {
		return sellAmount;
	}

	public long getBuyAmount() {
		return buyAmount;
	}

	public String toRawString() {
		if (getSellCurrency() == Currency.ROBUX)
			return getSellAmount() + " R$ for " + getBuyAmount() + " Tix @ 1:" + getRatioAB();
		else
			return getSellAmount() + " Tix for " + getBuyAmount() + " R$ @ " + getRatioBA() + ":1";
	}

	public String toString() {
		return "BUXTIXTradeOrder(" + toRawString() + ")";
	}

	private float getRatioBA() {
		return 1 / getRatioAB();
	}

	private float getRatioAB() {

		return (float) getBuyAmount() / (float) getSellAmount();
	}
}
