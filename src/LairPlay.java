import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class LairPlay {
	private final int basePort;
	private final AmplifierController controller;
	
	public LairPlay(File configuration) throws IOException {
		// jmdns uses java.util.logging enable jul-to-slf4j
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
		Properties config = getProperties(configuration);
		String type = config.getProperty("controllerType");
		if (type == null) throw new RuntimeException("missing controllerType");
		
		String temp = config.getProperty("basePort");
		if (temp == null) throw new RuntimeException("missing basePort");
		basePort = Integer.parseInt(temp);
		
		controller = type.equals("Russound") ? 
				new RussoundController(config) :
				new BaseController(config);
	}
	
	public void play() {
		Set<String> groups = controller.getGroups();
		List<Thread> threads = new ArrayList<>();
		
		int port = basePort;
		for (String group : groups) {
			Thread t = new GroupThread(group, port++, controller);
			t.start();
			threads.add(t);
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
