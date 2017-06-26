package roblox.model;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Order {

	static DecimalFormat availableFormat = new DecimalFormat("#,###");

	long available;
	boolean atMarket = false;
	float ratioA;
	float ratioB;

	public Order() {
	}

	public static Order parseOrder(String anOrderString) throws ParseException {
		Order newOrder = new Order();
		newOrder.parseOrderPart(anOrderString);
		return newOrder;
	}

	protected void parseOrderPart(String anOrderString) throws ParseException {
		String parsed[] = anOrderString.split(" @");
		available = (Long) availableFormat.parse(parsed[0]);
		// Determine if a Market order
		if (parsed[1].contains("Market")) {
			atMarket = true;
		} else {
			String ratio[] = parsed[1].split(":");
			ratioA = Float.parseFloat(ratio[0]);
			ratioB = Float.parseFloat(ratio[1]);
		}
	}

	public boolean isAtMarket() {
		return atMarket;
	}

	public String toString() {
		return available + " @ " + (atMarket ? "Market" : (ratioA + ":" + ratioB));
	}

	public static DecimalFormat getAvailableFormat() {
		return availableFormat;
	}

	public long getAvailable() {
		return available;
	}

	public float getRatioA() {
		return ratioA;
	}

	public float getRatioB() {
		return ratioB;
	}

}
