package com.lairplay.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.lairplay.comm.BaseResponder;
import com.lairplay.comm.EmptyResponder;
import com.lairplay.controller.AmplifierController;
import com.lairplay.controller.DummyController;
import com.lairplay.controller.RussoundController;

public class LairPlay {
	final Logger logger = LoggerFactory.getLogger(LairPlay.class);
	private final int basePort;
	private final AmplifierController controller;

	public LairPlay(File configuration) throws IOException {
		// jmdns uses java.util.logging enable jul-to-slf4j
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
		logger.info("starting LairPlay");

		Properties config = getProperties(configuration);
		String type = config.getProperty("controllerType");
		if (type == null) throw new RuntimeException("missing controllerType");
		
		String temp = config.getProperty("basePort");
		if (temp == null) throw new RuntimeException("missing basePort");
		basePort = Integer.parseInt(temp);

		logger.info("base port = " + basePort);
		
		controller = type.equals("Russound") ? 
				new RussoundController(config) :
				new DummyController(config);
	}
	
	public void play() {
		Set<String> groups = controller.getGroups();
		List<GroupThread> groupThreads = new ArrayList<>();
		BlockingQueue<BaseResponder> responderQueue = new ArrayBlockingQueue<>(1);
		
		int port = basePort;
		for (String group : groups) {
			GroupThread t = new GroupThread(group, port++, controller, responderQueue);
			t.start();
			groupThreads.add(t);
		}

		try {
			responderQueue.add(new EmptyResponder());
			
			while (true) {
				BaseResponder responder = responderQueue.take();
				if (! (responder instanceof EmptyResponder) ) {
					responder.join(100);
					if (!responder.isAlive()) {
						responder = new EmptyResponder();
						logger.info("responder thread finished");
					} 
				}
				responderQueue.add(responder);
				
				for (GroupThread thread : groupThreads) {
					thread.join(100);
				}
				
				Thread.sleep(1000);
			}
		}
			
		catch (InterruptedException e) {
			logger.error("what happened", e);
		}
	}
	
	/**
	 * 
	 * @param contiguration
	 * @return
	 * @throws IOException 
	 */
	private Properties getProperties(File configuration) throws IOException {
		InputStream input = new FileInputStream(configuration);
		Properties props = new Properties();
		props.load(input);
		return props;
	}
	
	public static void main(String[] args) throws IOException  {
		if (args.length != 1) {
			System.out.println("usage: lairplay <configuration.properties>");
			System.exit(0);
			
		}

		File configuration = new File(args[0]);
		LairPlay player = new LairPlay(configuration);
		player.play();
	}
}
