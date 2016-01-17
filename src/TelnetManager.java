import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TelnetManager extends Thread {
	final Logger logger = LoggerFactory.getLogger(TelnetManager.class);
	private final static int LIFE_SECS = 120;
	private final static int OVERLAP_SECS = 20;
	private final String hostname;
	private final int port;
	private final AtomicReference<TelnetClient> telnetHolder = new AtomicReference<TelnetClient>();
	private boolean done = false;
	
	public TelnetManager(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
		logger.info("opening telnet to " + hostname + ":" + port);
		telnetHolder.set(null);
	}
	
	/**
	 * 
	 * @return
	 */
	public TelnetClient getClient() {
		return telnetHolder.get();
	}
	
	public void close() throws IOException {
		TelnetClient client = telnetHolder.get();
		client.close();
		done = true;
	}
	
	@Override
	public void run() {
		while (!done) {
			try {
				Thread.sleep(LIFE_SECS * 1000);
			} catch (InterruptedException e) {
				logger.info("what happened?", e);
				return;
			}
		
			TelnetClient curr = telnetHolder.get();

			TelnetClient next;
			try {
				logger.debug("opening telnet to " + hostname + ":" + port);
				next = new TelnetClient(hostname, port);
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
				if (curr != null) {
					logger.debug("closing telnet to " + hostname + ":" + port);
					curr.close();
				}
			} catch (IOException e) {
				logger.info("what happened?", e);
				return;
			}
		}
	}
}
