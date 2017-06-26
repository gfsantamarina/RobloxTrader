package roblox.driver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import roblox.model.RobloxTradeState;

public class RobloxTrader {

	private static String BROWSER_PROFILE_PATH = "BrowserProfilePath";
	private static String OUTPUT_FILE_PATH = "OutputFilePath";

	public enum LoopControl {
		GO, PAUSE, PAUSE_TRADING, STOP, STOPPED
	};

	private LoopControl loopControl = LoopControl.GO;
	private RobloxTraderEventListener eventListener;

	Scanner input = new Scanner(System.in);

	public class RobloxTraderConfiguration {
		private String browserProfilePath;
		private String outputFilePath;

		protected RobloxTraderConfiguration(String browserProfilePath, String outputFilePath) {
			this.browserProfilePath = browserProfilePath;
			this.outputFilePath = outputFilePath;
		}

		public String getBrowserProfilePath() {
			return browserProfilePath;
		}

		public String getOutputFilePath() {
			return outputFilePath;
		}
	}

	public static RobloxTraderConfiguration readConfiguration(String aConfigurationFileName, RobloxTrader aTrader)
			throws IOException {
		FileReader aReader = null;
		Properties traderProperties = new Properties();
		try {
			aReader = new FileReader(new File(aConfigurationFileName));
			traderProperties.load(aReader);
			return aTrader.new RobloxTraderConfiguration((String) traderProperties.get(BROWSER_PROFILE_PATH),
					(String) traderProperties.get(OUTPUT_FILE_PATH));
		} finally {
			if (aReader != null)
				try {
					aReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private static void appendToCSVFile(FileWriter aWriter, RobloxTradeState aTradeState) throws IOException {
		try {
			aWriter.write(aTradeState.asCSVString());
			aWriter.append("\n");
		} finally {
			if (aWriter != null)
				aWriter.flush();
		}
	}

	// Spawn another thread to keep looping until 'q'<enter> is pressed
	private Thread getKeyboardThread() {
		return new Thread("KeyboardPoller") {
			public void run() {
				do {
					if (input.hasNext()) {
						String read = input.next();
						if (read.equals("p"))
							pause();
						if (read.equals("pt"))
							pauseTrading();
						if (read.equals("g"))
							go();
						if (read.equals("q"))
							quit();
					}
				} while (loopControl != LoopControl.STOP);

			}
		};
	}

	public void runTrader(RobloxTraderConfiguration config, boolean inConsoleMode) throws IOException {

		RobloxInterface aRobloxInterface = new RobloxInterface(config.getBrowserProfilePath());
		RobloxDecisionEngine aDecisionEngine = new RobloxDecisionEngine();
		RobloxTradeState aTradeState;
		File aCSVFile = new File(config.getOutputFilePath());
		FileWriter aCSVWriter = new FileWriter(aCSVFile, true);
		aCSVWriter.write(RobloxTradeState.csvHeaders());

		if (inConsoleMode)
			getKeyboardThread().start();

		try {
			do {
				if (loopControl == LoopControl.GO || loopControl == LoopControl.PAUSE_TRADING) {
					aTradeState = aRobloxInterface.getRobloxTradeState();
					System.out.println(aTradeState);
					if (getEventListener() != null)
						getEventListener().stateChanged(aTradeState);
					aDecisionEngine.decide(aTradeState);
					appendToCSVFile(aCSVWriter, aTradeState);
					if (loopControl != LoopControl.PAUSE_TRADING && aTradeState.isActionNeeded()) {
						aRobloxInterface.takeAction(aTradeState);
					}
				}
				if (loopControl != LoopControl.STOP)
					Thread.sleep(3000);
			} while (loopControl != LoopControl.STOP);
			loopControl = LoopControl.STOPPED;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				aCSVWriter.close();
				aRobloxInterface.shutDown();
				loopControl = LoopControl.STOPPED;
				if (getEventListener() != null)
					getEventListener().traderStopped();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException {

		RobloxTrader aTrader = new RobloxTrader();

		RobloxTraderConfiguration config = null;

		if (args.length == 2) {
			config = aTrader.new RobloxTraderConfiguration(args[0], args[1]);

		} else if (args.length == 1) {
			config = readConfiguration(args[0], aTrader);
		} else {
			System.err.println("Error: You need to pass in one or two parameters.");
			System.err.println("RobloxTrader pathToConfigurationFile  OR");
			System.err.println("RobloxTrader pathToBrowserProfile pathToFileOutputCSV.");
			System.exit(-1);
		}

		aTrader.runTrader(config, true);
	}

	public boolean isStopped() {
		return loopControl == LoopControl.STOPPED;
	}

	public void go() {
		loopControl = LoopControl.GO;
		System.out.println("Continuing...");
		if (getEventListener() != null)
			getEventListener().loopControlChanged(loopControl);
	}

	public void quit() {
		loopControl = LoopControl.STOP;
		System.out.println("Stopping...");
		if (getEventListener() != null)
			getEventListener().loopControlChanged(loopControl);
	}

	public void pauseTrading() {
		loopControl = LoopControl.PAUSE_TRADING;
		System.out.println("Pausing trading...");
		if (getEventListener() != null)
			getEventListener().loopControlChanged(loopControl);
	}

	public void pause() {
		loopControl = LoopControl.PAUSE;
		System.out.println("Pausing...");
		if (getEventListener() != null)
			getEventListener().loopControlChanged(loopControl);
	}

	public RobloxTraderEventListener getEventListener() {
		return eventListener;
	}

	public void setEventListener(RobloxTraderEventListener eventListener) {
		this.eventListener = eventListener;
	}

	public LoopControl getLoopControl() {
		return loopControl;
	}
}
