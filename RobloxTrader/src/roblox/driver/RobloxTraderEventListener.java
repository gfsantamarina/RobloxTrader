package roblox.driver;

import java.util.EventListener;

import roblox.driver.RobloxTrader.LoopControl;
import roblox.model.RobloxTradeState;

public interface RobloxTraderEventListener extends EventListener {

	public void traderStopped();

	void stateChanged(RobloxTradeState aTradeState);

	void loopControlChanged(LoopControl loopControlState);

}
