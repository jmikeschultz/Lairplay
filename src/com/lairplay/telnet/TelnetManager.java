package com.lairplay.telnet;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TelnetManager extends Thread {
	final Logger logger = LoggerFactory.getLogger(TelnetManager.class);
	private final static int LIFE_SECS = 120;
	private final String hostname;
	private final int port;
	private final BlockingQueue<TelnetClient> clients = new LinkedBlockingQueue<>();
	private boolean done = false;
	
	public TelnetManager(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}
	
	private TelnetClient create() {
		try {
			logger.debug("opening telnet to " + hostname + ":" + port);
			return new TelnetClient(hostname, port);
		} catch (IOException e) {
			logger.info("could not create telnet client", e);
		}

		return null;
	}
	
	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String writeln(String command) throws IOException, InterruptedException {
		TelnetClient telnet = getClient();
		telnet.writeln(command);
		String reply = telnet.readln();
		putBack(telnet);
		return reply;
	}
	
	/**
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	private TelnetClient getClient() throws InterruptedException {
		return clients.take();
	}
	
	private void putBack(TelnetClient client) {
		clients.add(client);
	}
	
	public void close() throws IOException {
		TelnetClient client = clients.poll();
		client.close();
		client = clients.poll();
		client.close();
		done = true;
	}
	
	@Override
	public void run() {
		int errors = 0;
		while (!done) {
			try {
				while (clients.size() < 2) {
					TelnetClient client = create();
					if (client == null) continue;
					clients.add(create());
					Thread.sleep(1000); // so one is older
				}
				
				Thread.sleep(LIFE_SECS * 1000);
			} catch (InterruptedException e) {
				logger.info("what happened?", e);
				return;
			}
		
			TelnetClient next;
			try {
				TelnetClient one = clients.take();
				TelnetClient two = clients.take();

				// poor man's priority queue
				if (one.ageSecs() > two.ageSecs()) {
					one.close();
					clients.add(two);
				} else {
					two.close();
					clients.add(one);
				}
				
				logger.debug("opening telnet to " + hostname + ":" + port);
				next = new TelnetClient(hostname, port);
			} catch (Exception e) {
				errors++;
				logger.info("could not establish telnet connection (attempt=" + errors + ")", e);
				continue; // try again
			}

			errors = 0;
			clients.add(next);
		}
	}
}
