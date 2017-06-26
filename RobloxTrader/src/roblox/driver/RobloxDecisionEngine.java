package roblox.driver;

import roblox.model.Order;
import roblox.model.RobloxTradeState;
import roblox.model.RobloxTradeState.Currency;
import roblox.model.BUXTIXTradeOrder;

public class RobloxDecisionEngine {

	int minTradeSpreadThreshold = -50;
	int maxTradeSpreadThreshold = 100;
	int maxROBUXSpreadThreshold = 400;
	int minTicketsSpreadThreshold = -400;

	public void decide(RobloxTradeState aTradeState) {
		// Check for no open positions
		if (!aTradeState.hasOpenROBUXPositions() && !aTradeState.hasOpenTicketsPositions()) {
			if (aTradeState.getRobuxBalance() > 0 && !aTradeState.isAnyTopOrderAtMarket()
					&& aTradeState.getSpread() > maxTradeSpreadThreshold
					&& aTradeState.getROBUXOrderSpread(0) < maxROBUXSpreadThreshold) {
				aTradeState.setBuyOrder(
						getTradeOrderToBeatPosition(aTradeState, Currency.ROBUX, aTradeState.getRobuxBalance(), 0));
			}
			if (aTradeState.getTicketsBalance() > 0 && !aTradeState.isAnyTopOrderAtMarket()
					&& aTradeState.getSpread() < minTradeSpreadThreshold
					&& aTradeState.getTicketsOrderSpread(0) > minTicketsSpreadThreshold) {
				aTradeState.setBuyOrder(
						getTradeOrderToBeatPosition(aTradeState, Currency.TICKETS, aTradeState.getTicketsBalance(), 0));
			}
		}

	}

	private BUXTIXTradeOrder getTradeOrderToBeatPosition(RobloxTradeState aTradeState, Currency sellCurrency,
			long sellAmount, int positionToBeat) {
		if (sellCurrency == Currency.ROBUX) {
			Order orderToBeat = aTradeState.getRobuxOrders().get(positionToBeat);
			float ticketsToBuy = (sellAmount * orderToBeat.getRatioB());
			return new BUXTIXTradeOrder(sellCurrency, sellAmount, (long) ticketsToBuy);
		} else {
			Order orderToBeat = aTradeState.getTicketOrders().get(positionToBeat);
			float robuxToBuy = (sellAmount / orderToBeat.getRatioA());
			return new BUXTIXTradeOrder(sellCurrency, sellAmount, (long) (robuxToBuy + 0.5));
		}

	}
}
