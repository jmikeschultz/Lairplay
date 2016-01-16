import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * GroupThread listens to the AirPort ports.  If a connection comes in
 * it starts a RTSPResponder thread to handle it.
 *
 */
public class GroupThread extends Thread {
	private final Logger logger = LoggerFactory.getLogger(GroupThread.class);
	private BonjourEmitter emitter;
	private	ServerSocket servSock = null;
	private boolean done = false;
	private final String name;
	private final int port;
	private final String password = null; // for now
	private final AmplifierController controller;
	/**
	 * 
	 * @param info 
	 * @param lock used to coordinate use of the singleton audio output
	 * @throws UnknownHostException 
	 */
	public GroupThread(String name, int port, AmplifierController controller) {
		super(name);
		this.name = name;
		this.port = port;
		this.controller = controller;
	}

	public int getPort() {
		return port;
	}

	public void run() {
		logger.info("starting group '" + name + "'");

		// Setup safe shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
   			@Override
   			public void run() {
   				logger.info("stopping group '" + name + "'");
   				GroupThread.this.stopThread();
   				
   				try {
					emitter.stop();
					servSock.close();
	    			logger.info("group '" + name + "' stopped.");
   				} catch (IOException e) {
					logger.error("what happened", e);
   				}
   			}
  		});
				
		
		try {
			InetAddress local = InetAddress.getLocalHost();
			emitter = new BonjourEmitter(name, port, null);
			byte[] randomId = emitter.getRandomId();
			
			// We listen for new connections
			servSock = getServerSocket();
						
			while (!done) {
				try {
					Socket socket = servSock.accept();
					
					// if we're connecting to ourself or the controller is unavailable
					if (socket.getInetAddress() == local || !controller.activateGroup(name)) {
						logger.info("controller unavailable for group: " + name);
						socket.close();
						continue;
					}

					logger.info("group '" + name + "' accepted connection from " + socket.toString());

					// Check if password is set
					Thread t = password != null ?
							new RTSPResponder(randomId, socket, password, controller) :
							new RTSPResponder(randomId, socket, controller);
					t.start();
					
					// wait for the thread to end
					while (t.isAlive()) {
						try {
							t.join(100);
						} catch (InterruptedException e) {
							logger.error("what happened", e);
						}
					}
					
					controller.deactivateGroup(name);
					logger.info("device disconnected from group '" + name + "'");
				} 
				
				catch(SocketTimeoutException e) {
					// loop
				}
			}

		} catch (IOException e) {
			try {
				logger.error("what happened", e);
				controller.deactivateGroup(name);
			} catch (IOException e1) {
				logger.error("what happened", e1);
			}
			
			throw new RuntimeException(e);
		} 
		
		finally {
			try {
				emitter.stop(); 
				servSock.close();
				controller.deactivateGroup(name);
			} catch (IOException e) {
				logger.error("what happened", e);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private ServerSocket getServerSocket() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			ss.setSoTimeout(1000);
			logger.info("group '" + name + "'  started.");
			
		} catch (IOException e) {
			logger.error("port " + port + " busy for group '" + name + "'");
			try {
				emitter.stop();
			} catch (IOException e1) {
				logger.error("what happened", e1);
			}

			done = true;
		}
		
		return ss;
	}
	
	public synchronized void stopThread() {
		done = true;
	}
}
