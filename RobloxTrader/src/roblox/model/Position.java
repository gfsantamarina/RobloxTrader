package roblox.model;

import java.text.ParseException;
import java.util.List;

public class Position extends Order {

	private class PositionAndCummulativeVolume {
		int position;
		long cummulativeVolume;

		public PositionAndCummulativeVolume(int position, long cummulativeVolume) {
			this.position = position;
			this.cummulativeVolume = cummulativeVolume;
		}

		public int getPosition() {
			return position;
		}

		public long getCummulativeVolume() {
			return cummulativeVolume;
		}

		public String toString() {
			return "position: " + getPosition() + " - cumm.volume: " + getCummulativeVolume();
		}
	}

	List<Order> orderList;
	long remainder;

	public Position() {
	}

	// Returns true if the Position matches a particular order with an order
	// index
	private boolean matchesOrder(int orderIndex, Order anOrder) {
		return anOrder.getAvailable() == getRemainder();
	}

	// Finds the order of the position within the list of orders
	public PositionAndCummulativeVolume getPositionAndCummulativeVolume() {
		int orderIndex = 0;
		long cummulativeVolume = 0;
		for (Order selectedOrder : getOrderList()) {
			if (matchesOrder(orderIndex, selectedOrder))
				return new PositionAndCummulativeVolume(orderIndex, cummulativeVolume);
			orderIndex++;
			cummulativeVolume += selectedOrder.getAvailable();
		}
		return null;
	}

	public static Position parsePosition(String aPositionString) throws ParseException {
		Position newPosition = new Position();
		String tokens[] = aPositionString.split(" ");
		newPosition.parseOrderPart(tokens[1] + " @" + tokens[4]);
		newPosition.remainder = (Long) availableFormat.parse(tokens[5]);
		return newPosition;
	}

	public List<Order> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<Order> orderList) {
		this.orderList = orderList;
	}

	public long getRemainder() {
		return remainder;
	}

	public String toString() {
		PositionAndCummulativeVolume aPositionAndCummulativeVolume = getPositionAndCummulativeVolume();
		return super.toString() + " - remainder: " + remainder + " - "
				+ (aPositionAndCummulativeVolume != null ? aPositionAndCummulativeVolume : "Position Not Found");
	}
}
