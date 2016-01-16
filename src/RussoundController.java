import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RussoundController extends BaseController {
	final Logger logger = LoggerFactory.getLogger(RussoundController.class);
	public final static int LOCK_AVAILABLE = Integer.MIN_VALUE;
	private final AtomicInteger lock = new AtomicInteger();
	private final static int RUSSOUND_TELNET_PORT = 9621;
	private final static int MAC_MINI_SOURCE = 2;
	private final AtomicReference<TelnetClient> telnetHolder = new AtomicReference<TelnetClient>();
	
	private final Thread maintainer;
	private final static int LIFE_SECS = 120;
	private final static int OVERLAP_SECS = 20;

	/**
	 * 
	 * @param hostname like MCAC5-006B08.local from router device list
	 * @throws IOException 
	 */
	public RussoundController(Properties config) throws IOException {
		super(config);
		logger.info("starting RussoundController");

		lock.set(LOCK_AVAILABLE);

		logger.debug("opening telnet to " + hostname + ":" + RUSSOUND_TELNET_PORT);
		TelnetClient telnet = new TelnetClient(hostname, RUSSOUND_TELNET_PORT);
		
		telnetHolder.set(telnet);
		
		/**
		 * maintainer keeps 2 telnets open to the controller
		 * and expires them after LIFE_SECS;
		 */
		maintainer = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(LIFE_SECS * 1000);
					} catch (InterruptedException e) {
						logger.info("what happened?", e);
						return;
					}
				
					TelnetClient curr = telnetHolder.get();

					TelnetClient next;
					try {
						logger.debug("opening telnet to " + hostname + ":" + RUSSOUND_TELNET_PORT);
						next = new TelnetClient(hostname, RUSSOUND_TELNET_PORT);
					} catch (IOException e) {
						logger.info("what happened?", e);
						return;
					}
					telnetHolder.set(next);

					// keep curr around for late commands
					try {
						Thread.sleep(OVERLAP_SECS * 1000);
					} catch (InterruptedException e) {
						logger.info("what happened?", e);
						return;
					}
				
					try {
						logger.debug("closing telnet to " + hostname + ":" + RUSSOUND_TELNET_PORT);
						curr.close();
					} catch (IOException e) {
						logger.info("what happened?", e);
						return;
					}
				}
			}
		};
		
		maintainer.start();
	}

	@Override
	public void close() throws IOException {
		logger.info("--- closing");
		TelnetClient telnet = telnetHolder.get();
		telnet.close();
		// stop the maintainer
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	private synchronized String execute(String command) throws IOException {
		TelnetClient telnet = telnetHolder.get();
		telnet.writeln(command);
		return telnet.readln();
	}

	@Override
	public synchronized boolean activateGroup(String name) throws IOException {
		boolean gotIt = lock.compareAndSet(LOCK_AVAILABLE, name.hashCode());
		if (!gotIt) return false;
		
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
		lock.compareAndSet(name.hashCode(), LOCK_AVAILABLE);
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
