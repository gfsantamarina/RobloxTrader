package roblox.model;

import java.util.Date;
import java.util.List;

public class RobloxTradeState {

	public enum Currency {
		ROBUX, TICKETS
	};

	// Inputs
	Date snapshotTaken;
	long robuxBalance;
	long ticketsBalance;
	long spread;
	List<Order> robuxOrders;
	List<Order> ticketOrders;
	List<Position> openROBUXPositions;
	List<Position> openTicketsPositions;

	// Outputs
	BUXTIXTradeOrder buyOrder;

	public RobloxTradeState(Date snapshotTaken, long robuxBalance, long ticketsBalance, long spread,
			List<Order> robuxOrders, List<Order> ticketOrders, List<Position> openROBOXPositions,
			List<Position> openTicketsPositions) {
		this.snapshotTaken = snapshotTaken;
		this.robuxBalance = robuxBalance;
		this.ticketsBalance = ticketsBalance;
		this.spread = spread;
		this.robuxOrders = robuxOrders;
		for (Position aPosition : openROBOXPositions)
			aPosition.setOrderList(robuxOrders);
		this.ticketOrders = ticketOrders;
		for (Position aPosition : openTicketsPositions)
			aPosition.setOrderList(ticketOrders);
		this.openROBUXPositions = openROBOXPositions;
		this.openTicketsPositions = openTicketsPositions;
		for (Position aPosition : openROBOXPositions)
			aPosition.setOrderList(robuxOrders);
	}

	public Date getSnapshotTaken() {
		return snapshotTaken;
	}

	public long getRobuxBalance() {
		return robuxBalance;
	}

	public long getTicketsBalance() {
		return ticketsBalance;
	}

	public List<Order> getRobuxOrders() {
		return robuxOrders;
	}

	public List<Order> getTicketOrders() {
		return ticketOrders;
	}

	public List<Position> getOpenROBUXPositions() {
		return openROBUXPositions;
	}

	public List<Position> getOpenTicketsPositions() {
		return openTicketsPositions;
	}

	@SuppressWarnings("rawtypes")
	private StringBuffer dumpArrayToStringBuffer(StringBuffer aBuffer, String prefix, List aList, int len) {
		if (!aList.isEmpty()) {
			aBuffer.append(prefix);
			for (int i = 0; i < aList.size() && i < len; i++)
				aBuffer.append(" [" + aList.get(i) + "], ");
			aBuffer.append("\n");
		} else {
			aBuffer.append("None, ");
		}
		return aBuffer;
	}

	public String toString() {
		StringBuffer aBuffer = new StringBuffer();
		aBuffer.append("RobloxTradeState @ " + getSnapshotTaken() + "\n");
		aBuffer.append("ROBUXBalance: " + getRobuxBalance() + "\n");
		aBuffer.append("TicketsBalance: " + getTicketsBalance() + "\n");
		dumpArrayToStringBuffer(aBuffer, "Ticket Orders: ", getTicketOrders(), 5);
		dumpArrayToStringBuffer(aBuffer, "ROBUXOrders:", getRobuxOrders(), 5);
		if (hasOpenROBUXPositions()) {
			dumpArrayToStringBuffer(aBuffer, "Open ROBUX Positions:", getOpenROBUXPositions(), 5);
		} else {
			aBuffer.append("No ROBUX Positions\n");
		}
		if (hasOpenTicketsPositions()) {
			dumpArrayToStringBuffer(aBuffer, "Open Tickets Positions:", getOpenTicketsPositions(), 5);
		} else {
			aBuffer.append("No Tickets Positions\n");
		}
		aBuffer.append("Spread: " + getSpread() + "\n");
		return aBuffer.toString();
	}

	public boolean hasOpenROBUXPositions() {
		return !getOpenROBUXPositions().isEmpty();
	}

	public boolean hasOpenTicketsPositions() {
		return !getOpenTicketsPositions().isEmpty();
	}

	public void calculateProfitableTrade() {

	}

	// How long has it been since the snapshot was taken in seconds
	public long getSnapshotAge() {
		return (new Date().getTime() - getSnapshotTaken().getTime()) / 1000;
	}

	public long getSpread() {
		return spread;
	}

	private long getOrderSpread(List<Order> anOrderList, int baseOrderIndex) {
		if (baseOrderIndex < 0)
			new Exception("RobloxTradeState.getOrderSpread() - baseOrderIndex must be more than zero");

		Order referenceOrder = anOrderList.get(baseOrderIndex);
		Order compareToOrder = anOrderList.get(baseOrderIndex + 1);

		if (compareToOrder.getRatioA() != 1) {
			return (long) (((compareToOrder.getRatioA() - referenceOrder.getRatioA()) * 1000) - 0.5);
		} else {
			return (long) (((compareToOrder.getRatioB() - referenceOrder.getRatioB()) * 1000) + 0.5);
		}
	}

	public long getROBUXOrderSpread(int baseOrderIndex) {
		return getOrderSpread(getRobuxOrders(), baseOrderIndex);
	}

	public long getTicketsOrderSpread(int baseOrderIndex) {
		return getOrderSpread(getTicketOrders(), baseOrderIndex);
	}

	public boolean isROBUXTopOrderAtMarket() {
		return getRobuxOrders().get(0).isAtMarket();
	}

	public boolean isTicketsTopOrderAtMarket() {
		return getTicketOrders().get(0).isAtMarket();
	}

	public boolean isAnyTopOrderAtMarket() {
		return isROBUXTopOrderAtMarket() || isTicketsTopOrderAtMarket();
	}

	public boolean isActionNeeded() {
		return buyOrder != null;
	}

	public BUXTIXTradeOrder getBuyOrder() {
		return buyOrder;
	}

	// Finds the position of a currency and index
	public int getPositionOrder(List<Order> orders, Position aPosition) {
		int i = 0;
		for (Order selectedOrder : orders) {
			if (selectedOrder.getAvailable() == aPosition.getRemainder() && selectedOrder.ratioA == aPosition.ratioA
					&& selectedOrder.ratioB == aPosition.ratioB) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public void setBuyOrder(BUXTIXTradeOrder buyOrder) {
		this.buyOrder = buyOrder;
	}

	@SuppressWarnings("rawtypes")
	private StringBuffer dumpForCSVStringBuffer(StringBuffer aBuffer, List aList, int len) {
		if (!aList.isEmpty()) {
			for (int i = 0; i < aList.size() && i < len; i++)
				aBuffer.append("\"" + aList.get(i) + "\",");
		} else {
			aBuffer.append("None, ");
		}
		return aBuffer;
	}

	private static StringBuffer dumpForCSVHeadersForList(StringBuffer aBuffer, String listPrefix, int len) {
		for (int i = 0; i < len; i++)
			aBuffer.append(listPrefix + "[" + i + "],");
		return aBuffer;
	}

	public static char[] csvHeaders() {
		StringBuffer aBuffer = new StringBuffer();
		aBuffer.append("Snapshot taken, ");
		aBuffer.append("ROBUX, ");
		aBuffer.append("Tickets, ");
		// Only dump the first five orders and positions
		dumpForCSVHeadersForList(aBuffer, "TicketsOrder", 5);
		dumpForCSVHeadersForList(aBuffer, "RobuxOrder", 5);
		dumpForCSVHeadersForList(aBuffer, "ROBUXPosition", 2);
		dumpForCSVHeadersForList(aBuffer, "TicketPosition", 2);
		aBuffer.append("Spread, ");
		aBuffer.append("BuyOrder");
		aBuffer.append("\n");
		char[] returnChars = new char[aBuffer.length()];
		aBuffer.getChars(0, aBuffer.length(), returnChars, 0);
		return returnChars;
	}

	public char[] asCSVString() {
		StringBuffer aBuffer = new StringBuffer();
		aBuffer.append(getSnapshotTaken() + ", ");
		aBuffer.append(getRobuxBalance() + ", ");
		aBuffer.append(getTicketsBalance() + ", ");
		// Only dump the first five orders and positions
		dumpForCSVStringBuffer(aBuffer, getTicketOrders(), 5);
		dumpForCSVStringBuffer(aBuffer, getRobuxOrders(), 5);
		dumpForCSVStringBuffer(aBuffer, getOpenROBUXPositions(), 2);
		dumpForCSVStringBuffer(aBuffer, getOpenTicketsPositions(), 2);
		aBuffer.append(getSpread() + ", ");
		aBuffer.append(getBuyOrder() != null ? getBuyOrder().toRawString() : "None");
		char[] returnChars = new char[aBuffer.length()];
		aBuffer.getChars(0, aBuffer.length(), returnChars, 0);
		return returnChars;
	}
}
