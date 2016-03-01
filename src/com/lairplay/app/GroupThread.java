package com.lairplay.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lairplay.comm.BaseResponder;
import com.lairplay.comm.BonjourEmitter;
import com.lairplay.comm.EmptyResponder;
import com.lairplay.comm.RTSPResponder;
import com.lairplay.controller.AmplifierController;



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
	private final BlockingQueue<BaseResponder> responderQueue;
	/**
	 * 
	 * @param info 
	 * @param lock used to coordinate use of the singleton audio output
	 * @throws UnknownHostException 
	 */
	public GroupThread(String name, int port, AmplifierController controller, BlockingQueue<BaseResponder> reponderQueue) {
		super(name);
		this.name = name;
		this.port = port;
		this.controller = controller;
		this.responderQueue = reponderQueue;
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
			// airplay publicizing
			emitter = new BonjourEmitter(name, port, null);
			
			// We listen for new connections
			servSock = getServerSocket();
						
			while (!done) {
				try {
					Socket socket = servSock.accept();  // block
					
					BaseResponder responder = responderQueue.take();
					if (! (responder instanceof EmptyResponder) ){
						responder.stopResponder();
						responder.join();
						logger.info("responder thread finished");
					}

					logger.info("group '" + name + "' accepted connection from " + socket.toString());

					// Check if password is set
					responder = password != null ?
							new RTSPResponder(emitter.getRandomId(), socket, password, controller, name) :
							new RTSPResponder(emitter.getRandomId(), socket, controller, name);
					responder.start();
					
					responderQueue.add(responder);
				} 
				
				catch(SocketTimeoutException e) {
					// loop
				}
			}

		} catch (IOException | InterruptedException e) {
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
				if (servSock != null) {
					servSock.close();
				}
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
