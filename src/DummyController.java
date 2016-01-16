import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DummyController extends BaseController {
	final Logger logger = LoggerFactory.getLogger(DummyController.class);

	public DummyController(Properties config) throws IOException {
		super(config);
		logger.info("starting DummyController");
	}
	
	@Override
	public void close() throws IOException {
		logger.info("--- closing");
	}
	
	@Override
	public synchronized boolean activateGroup(String name) throws IOException {
		logger.info("--- activating group:" + name);
		return true;
	}
	
	@Override
	public synchronized boolean deactivateGroup(String name) throws IOException {
		logger.info("--- deactivating group:" + name);
		return true;
	}
	
	@Override
	public synchronized boolean setVolume(float knobVolume) throws IOException {
		logger.info("setting knob volume =" + knobVolume);
		return true;
	}
}
