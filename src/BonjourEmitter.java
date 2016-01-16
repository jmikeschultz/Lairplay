import java.util.*;

import javax.jmdns.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Emetteur Bonjour pour qu'iTunes detecte la borne airport
 * @author bencall
 *
 */

//
public class BonjourEmitter {
	final static int HOUSE_SYMBOL = 0x2302;
	// causes all the zones to show up together on iphone
	final static String house = new String(Character.toChars(HOUSE_SYMBOL));
	private final Logger logger = LoggerFactory.getLogger(BonjourEmitter.class);
	private final byte[] randId = new byte[6]; // based on mac addr length
	
	private final JmDNS jmdns;

	public BonjourEmitter(String name, int port, String password) throws IOException {
		name = house + " " + name;
		setRandomId(name);
		Map<String,Object> txtRec = new HashMap<String,Object>();
		// Set up TXT Record	    
		txtRec.put("txtvers", "1");
		txtRec.put("pw", String.valueOf(password != null));
		txtRec.put("sr", "44100");
		txtRec.put("ss", "16");
		txtRec.put("ch", "2");
		txtRec.put("tp", "UDP");
		txtRec.put("sm", "false");
		txtRec.put("sv", "false");
		txtRec.put("ek", "1");
		txtRec.put("et", "0,1");
		txtRec.put("cn", "0,1");
		txtRec.put("vn", "3");
		    		   
		// Zeroconf registration		
		jmdns = JmDNS.create();
		String identifier = toHexString(randId);
		ServiceInfo serviceInfo = ServiceInfo.create("_raop._tcp.local.", identifier + "@" + name, port, 0, 0, txtRec);
		jmdns.registerService(serviceInfo);				
		logger.info(String.format("announced [%s@%s:%d]", name, identifier, port));
	}
	
	/**
	 * 
	 * @return
	 */
	public byte[] getRandomId() {
		return randId;
	}
	
	/**
	 * Stop service publishing
	 */
	public void stop() throws IOException {
		jmdns.unregisterAllServices();
		jmdns.close();
	}
	
	/**
	 * 
	 * @param string
	 */
	private void setRandomId(String string) {
		int hash = string.hashCode();
		Random rand = new Random(hash);
		rand.nextBytes(randId);
	}
	
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	private String toHexString(byte[] bytes) {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) {
	      sb.append(String.format("%02x", b));
	    }
	      
	    return sb.toString();
	}
}

