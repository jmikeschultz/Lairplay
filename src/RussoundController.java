import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RussoundController extends BaseController {
	final Logger logger = LoggerFactory.getLogger(RussoundController.class);
	private final static int RUSSOUND_TELNET_PORT = 9621;
	private final static int MAC_MINI_SOURCE = 2;
	private final TelnetManager telnetManager;
	
	/**
	 * 
	 * @param hostname like MCAC5-006B08.local from router device list
	 * @throws IOException 
	 */
	public RussoundController(Properties config) throws IOException {
		super(config);
		logger.info("starting RussoundController");
		telnetManager = new TelnetManager(hostname, RUSSOUND_TELNET_PORT);
		telnetManager.start();
	}

	@Override
	public void close() throws IOException {
		logger.info("--- closing");
		telnetManager.close();
		try {
			telnetManager.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	private synchronized String execute(String command) throws IOException {
		TelnetClient telnet = telnetManager.getClient();
		telnet.writeln(command);
		return telnet.readln();
	}

	@Override
	public synchronized boolean activateGroup(String name) throws IOException {
		activeZones = groups.get(name);
		if (activeZones == null) {
			logger.info("--- unknown group:" + name);
			return false;
		}
		
		logger.info("--- activating group:" + name);
		
		for (Integer zone : activeZones) {
			// select source
			String command = String.format("EVENT C[1].Z[%d]!SelectSource %d", zone, MAC_MINI_SOURCE);
			String response = execute(command);
			logger.info("command=" + command + " response=" + response);

			// turn on
			command = String.format("EVENT C[1].Z[%d]!ZoneOn", zone);
			response = execute(command);
			logger.info("command=" + command + " response=" + response);
		}

		return true;
	}
	
	@Override
	public synchronized boolean deactivateGroup(String name) throws IOException {
		activeZones = groups.get(name);
		if (activeZones == null) {
			logger.info("--- unknown group:" + name);
			return false;
		}
		
		logger.info("--- deactivating group:" + name);
		for (Integer zone : activeZones) {
			// turn off
			String command = String.format("EVENT C[1].Z[%d]!ZoneOff", zone);
			String response = execute(command);
			logger.info("command=" + command + " response=" + response);
		}

		return true;
	}
	
	@Override
	public synchronized boolean setVolume(float knobVolume) throws IOException {
		int setVolume = knobVolume > 0.0F ? 
				(int)(minVolume + (maxVolume - minVolume) * knobVolume) : 0;

		for (Integer zone : activeZones) {
			String command = String.format("SET C[1].Z[%d].turnOnVolume=\"%d\"", zone, setVolume);
			String response = execute(command);
			logger.info("command=" + command + " response=" + response);
		}

		return true;
	}
}
