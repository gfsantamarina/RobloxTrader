package roblox.driver;

import java.util.EventListener;

import roblox.driver.RobloxTrader.LoopControl;
import roblox.model.RobloxTradeState;

public interface RobloxTraderEventListener extends EventListener {

	public void traderStopped();

	public void stateChanged(RobloxTradeState aTradeState);

	public void loopControlChanged(LoopControl loopControlState);

}
