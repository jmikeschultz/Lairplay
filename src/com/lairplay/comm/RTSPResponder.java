package com.lairplay.comm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyPair;
import java.security.Security;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import java.security.MessageDigest;
import java.math.BigInteger;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lairplay.audio.AudioServer;
import com.lairplay.audio.AudioSession;
import com.lairplay.comm.BaseResponder;
import com.lairplay.controller.AmplifierController;


/**
 * An primitive RTSP responder for replying iTunes
 * @author bencall
 *
 */
public class RTSPResponder extends BaseResponder {
	final Logger logger = LoggerFactory.getLogger(RTSPResponder.class);
	private final AmplifierController controller;
	private Socket socket;					// Connected socket
	private int[] fmtp;
	private byte[] aesiv, aeskey;			// ANNOUNCE request infos
	private AudioServer audioServer; 		// Audio listener
	byte[] uniqId;
	private BufferedReader in;
	private String password;
	private RTSPResponse response;
	private final String group; 
	
	/*
	private volatile boolean done = false;
	
	// Pre-define patterns
	private static final Pattern authPattern = Pattern.compile("Digest username=\"(.*)\", realm=\"(.*)\", nonce=\"(.*)\", uri=\"(.*)\", response=\"(.*)\"");
	private static final Pattern completedPacket = Pattern.compile("(.*)\r\n\r\n");

	
	private static final String key =  
		"-----BEGIN RSA PRIVATE KEY-----\n"
		+"MIIEpQIBAAKCAQEA59dE8qLieItsH1WgjrcFRKj6eUWqi+bGLOX1HL3U3GhC/j0Qg90u3sG/1CUt\n"
		+"wC5vOYvfDmFI6oSFXi5ELabWJmT2dKHzBJKa3k9ok+8t9ucRqMd6DZHJ2YCCLlDRKSKv6kDqnw4U\n"
		+"wPdpOMXziC/AMj3Z/lUVX1G7WSHCAWKf1zNS1eLvqr+boEjXuBOitnZ/bDzPHrTOZz0Dew0uowxf\n"
		+"/+sG+NCK3eQJVxqcaJ/vEHKIVd2M+5qL71yJQ+87X6oV3eaYvt3zWZYD6z5vYTcrtij2VZ9Zmni/\n"
		+"UAaHqn9JdsBWLUEpVviYnhimNVvYFZeCXg/IdTQ+x4IRdiXNv5hEewIDAQABAoIBAQDl8Axy9XfW\n"
		+"BLmkzkEiqoSwF0PsmVrPzH9KsnwLGH+QZlvjWd8SWYGN7u1507HvhF5N3drJoVU3O14nDY4TFQAa\n"
		+"LlJ9VM35AApXaLyY1ERrN7u9ALKd2LUwYhM7Km539O4yUFYikE2nIPscEsA5ltpxOgUGCY7b7ez5\n"
		+"NtD6nL1ZKauw7aNXmVAvmJTcuPxWmoktF3gDJKK2wxZuNGcJE0uFQEG4Z3BrWP7yoNuSK3dii2jm\n"
		+"lpPHr0O/KnPQtzI3eguhe0TwUem/eYSdyzMyVx/YpwkzwtYL3sR5k0o9rKQLtvLzfAqdBxBurciz\n"
		+"aaA/L0HIgAmOit1GJA2saMxTVPNhAoGBAPfgv1oeZxgxmotiCcMXFEQEWflzhWYTsXrhUIuz5jFu\n"
		+"a39GLS99ZEErhLdrwj8rDDViRVJ5skOp9zFvlYAHs0xh92ji1E7V/ysnKBfsMrPkk5KSKPrnjndM\n"
		+"oPdevWnVkgJ5jxFuNgxkOLMuG9i53B4yMvDTCRiIPMQ++N2iLDaRAoGBAO9v//mU8eVkQaoANf0Z\n"
		+"oMjW8CN4xwWA2cSEIHkd9AfFkftuv8oyLDCG3ZAf0vrhrrtkrfa7ef+AUb69DNggq4mHQAYBp7L+\n"
		+"k5DKzJrKuO0r+R0YbY9pZD1+/g9dVt91d6LQNepUE/yY2PP5CNoFmjedpLHMOPFdVgqDzDFxU8hL\n"
		+"AoGBANDrr7xAJbqBjHVwIzQ4To9pb4BNeqDndk5Qe7fT3+/H1njGaC0/rXE0Qb7q5ySgnsCb3DvA\n"
		+"cJyRM9SJ7OKlGt0FMSdJD5KG0XPIpAVNwgpXXH5MDJg09KHeh0kXo+QA6viFBi21y340NonnEfdf\n"
		+"54PX4ZGS/Xac1UK+pLkBB+zRAoGAf0AY3H3qKS2lMEI4bzEFoHeK3G895pDaK3TFBVmD7fV0Zhov\n"
		+"17fegFPMwOII8MisYm9ZfT2Z0s5Ro3s5rkt+nvLAdfC/PYPKzTLalpGSwomSNYJcB9HNMlmhkGzc\n"
		+"1JnLYT4iyUyx6pcZBmCd8bD0iwY/FzcgNDaUmbX9+XDvRA0CgYEAkE7pIPlE71qvfJQgoA9em0gI\n"
		+"LAuE4Pu13aKiJnfft7hIjbK+5kyb3TysZvoyDnb3HOKvInK7vXbKuU4ISgxB2bB3HcYzQMGsz1qJ\n"
		+"2gG0N5hvJpzwwhbhXqFKA4zaaSrw622wDniAK5MlIE0tIAKKP4yxNGjoD2QYjhBGuhvkWKaXTyY=\n"
		+"-----END RSA PRIVATE KEY-----\n"; */ 

	/**
	 * 
	 * @param uniqId
	 * @param socket
	 * @throws IOException
	 */
	public RTSPResponder(byte[] uniqId, Socket socket, AmplifierController controller, String group) throws IOException {
		this.controller = controller;
		this.uniqId = uniqId;
		this.socket = socket;
		this.group = group;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	/**
	 * 
	 * @param uniqId
	 * @param socket
	 * @param pass
	 * @throws IOException
	 */
	public RTSPResponder(byte[] uniqId, Socket socket, String pass, AmplifierController controller, String group) throws IOException {
		this.controller = controller;
		this.uniqId = uniqId;
		this.socket = socket;
		this.password = pass;
		this.group = group;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public synchronized void stopResponder() {
		done = true;
	}
	
	public RTSPResponse handlePacket(RTSPPacket packet) {
		if(password == null) {
			// No pass = ok!
			response = new RTSPResponse("RTSP/1.0 200 OK");
			response.append("Audio-Jack-Status", "connected; type=analog");
			response.append("CSeq", packet.valueOfHeader("CSeq"));
		} else {
			// Default response (deny, deny, deny!)
			response = new RTSPResponse("RTSP/1.0 401 UNAUTHORIZED");
			response.append("WWW-Authenticate", "Digest realm=\"*\" nonce=\"*\"");
			response.append("Method", "DENIED");

			String authRaw = packet.valueOfHeader("Authorization");
			
			// If supplied, check response
			if(authRaw != null) {
	        	Matcher auth = authPattern.matcher(authRaw);
	        
	        	if (auth.find()) {
	   	    		String username = auth.group(1);
	   	 	   		String realm = auth.group(2);
	   	   		 	String nonce = auth.group(3);
	   	    		String uri = auth.group(4);
	   	    		String resp = auth.group(5);
	  	          	String method = packet.getReq();
	  	          	
	  	          	String hash1 = md5Hash(username+":"+realm+":"+password).toUpperCase();
	  	          	String hash2 = md5Hash(method+":"+uri).toUpperCase();
	  	          	String hash = md5Hash(hash1+":"+nonce+":"+hash2).toUpperCase();
	  	          	
	  	          	// Check against password
	  	          	if(hash.equals(resp)) {
						// Success!
	  	          		response = new RTSPResponse("RTSP/1.0 200 OK");
						response.append("Audio-Jack-Status", "connected; type=analog");
						response.append("CSeq", packet.valueOfHeader("CSeq"));
	  	          	}
	        	}
			}
		}
        
		// Apple Challenge-Response field if needed
    	String challenge;
    	if( (challenge = packet.valueOfHeader("Apple-Challenge")) != null){
    		// BASE64 DECODE
    		byte[] decoded = Base64.decodeBase64(challenge);

    		// IP byte array
    		//byte[] ip = socket.getLocalAddress().getAddress();
    		SocketAddress localAddress = socket.getLocalSocketAddress(); //.getRemoteSocketAddress();
    		    		
    		byte[] ip =  ((InetSocketAddress) localAddress).getAddress().getAddress();
    		
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		// Challenge
    		try {
				out.write(decoded);
				// IP-Address
				out.write(ip);
				// HW-Addr
				out.write(uniqId);

				// Pad to 32 Bytes
				int padLen = 32 - out.size();
				for(int i = 0; i < padLen; ++i) {
					out.write(0x00);
				}

			} catch (IOException e) {
				logger.error("what happened", e);
			}
    		 
    		// RSA
    		byte[] crypted = this.encryptRSA(out.toByteArray());
    		
    		// Encode64
    		String ret = Base64.encodeBase64String(crypted);
    		
    		// On retire les ==
	        ret = ret.replace("=", "").replace("\r", "").replace("\n", "");

    		// Write
        	response.append("Apple-Response", ret);
    	} 
    	
		// Paquet request
		String REQ = packet.getReq();
        if(REQ.contentEquals("OPTIONS")){
        	// The response field
        	response.append("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");

        } else if (REQ.contentEquals("ANNOUNCE")){
        	// Nothing to do here. Juste get the keys and values
        	Pattern p = Pattern.compile("^a=([^:]+):(.+)", Pattern.MULTILINE);
        	Matcher m = p.matcher(packet.getContent());
        	while(m.find()){
        		if(m.group(1).contentEquals("fmtp")){
        			// Parse FMTP as array
        			String[] temp = m.group(2).split(" ");
        			fmtp = new int[temp.length];
        			for (int i = 0; i< temp.length; i++){
        				fmtp[i] = Integer.valueOf(temp[i]);
        			}
        			
        		} else if(m.group(1).contentEquals("rsaaeskey")){
        			aeskey = this.decryptRSA(Base64.decodeBase64(m.group(2)));
        		} else if(m.group(1).contentEquals("aesiv")){
        			aesiv = Base64.decodeBase64(m.group(2));
        		}
        	}
        	
        } else if (REQ.contentEquals("SETUP")){
        	int controlPort = 0;
        	int timingPort = 0;
        	
        	String value = packet.valueOfHeader("Transport");        	
        	
        	// Control port
        	Pattern p = Pattern.compile(";control_port=(\\d+)");
        	Matcher m = p.matcher(value);
        	if(m.find()){
        		controlPort =  Integer.valueOf(m.group(1));
        	}
        	
        	// Timing port
        	p = Pattern.compile(";timing_port=(\\d+)");
        	m = p.matcher(value);
        	if(m.find()){
        		timingPort =  Integer.valueOf(m.group(1));
        	}
            
        	// Launching audioserver
			audioServer = new AudioServer(new AudioSession(aesiv, aeskey, fmtp, controlPort, timingPort));

			// fixed volume, acting as line out signal
			float knobVolume = controller.getLineOutKnobVolume();
			audioServer.setVolume(VolumeTranslator.getRaopVolume(knobVolume));

        	response.append("Transport", packet.valueOfHeader("Transport") + ";server_port=" + audioServer.getServerPort());
        			
        	// ??? Why ???
        	response.append("Session", "DEADBEEF");
        } else if (REQ.contentEquals("RECORD")){
//        	Headers	
//        	Range: ntp=0-
//        	RTP-Info: seq={Note 1};rtptime={Note 2}
//        	Note 1: Initial value for the RTP Sequence Number, random 16 bit value
//        	Note 2: Initial value for the RTP Timestamps, random 32 bit value

        } else if (REQ.contentEquals("FLUSH")){
        	audioServer.flush();
        
        } else if (REQ.contentEquals("TEARDOWN")){
        	response.append("Connection", "close");
        	
        } else if (REQ.contentEquals("SET_PARAMETER")){
        	// Timing port
        	Pattern p = Pattern.compile("volume: (.+)");
        	Matcher m = p.matcher(packet.getContent());
        	if(m.find()){
        		String packetValue = m.group(1);
        		float raopVolume = Float.parseFloat(packetValue);
        		float knobVolume = VolumeTranslator.getKnobVolume(raopVolume);

        		logger.info("raop volume: " + raopVolume + " knob volume: " + knobVolume);

        		//audioServer.setVolume(raopVolume);
                try {
                	controller.setVolume(knobVolume);
                }
                catch (IOException e) {
                	logger.info("could not adjust volume");                		
                }
        	}
        	
        } else {
        	logger.info("REQUEST(" + REQ + "): Not Supported Yet!");
        	logger.debug(packet.getRawPacket());
        }
        
    	// We close the response
    	response.finalize();
    	return response;
	}

	/**
	 * Generates md5 hash of a string.
	 * @param plaintext string
	 * @return hash string
	 */
	public String md5Hash(String plaintext) {
		String hashtext = "";
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plaintext.getBytes());
			byte[] digest = md.digest();
		
			BigInteger bigInt = new BigInteger(1,digest);
			hashtext = bigInt.toString(16);
		
			// Now we need to zero pad it if you actually want the full 32 chars.
			while(hashtext.length() < 32 ) {
  				hashtext = "0"+hashtext;
			}
		} catch(java.security.NoSuchAlgorithmException e) {
			//
		}
		
		return hashtext;
	}

	/**
	 * Crypts with private key
	 * @param array	data to encrypt
	 * @return encrypted data
	 *
	public byte[] encryptRSA(byte[] array){
		try{
			Security.addProvider(new BouncyCastleProvider());

	        PEMReader pemReader = new PEMReader(new StringReader(key)); 
	        KeyPair pObj = (KeyPair) pemReader.readObject(); 

	        // Encrypt
	        Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding"); 
	        cipher.init(Cipher.ENCRYPT_MODE, pObj.getPrivate());
	        return cipher.doFinal(array);

		}catch(Exception e) {
			logger.error("what happened", e);
		}

		return null;
	}*/

	/**
	 * Decrypt with RSA priv key
	 * @param array
	 * @return
	 *
	public byte[] decryptRSA(byte[] array){
		try{
			Security.addProvider(new BouncyCastleProvider());

			// La clef RSA
	        PEMReader pemReader = new PEMReader(new StringReader(key)); 
	        KeyPair pObj = (KeyPair) pemReader.readObject(); 

	        // Encrypt
	        Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPadding"); 
	        cipher.init(Cipher.DECRYPT_MODE, pObj.getPrivate());
	        return cipher.doFinal(array);

		}catch(Exception e){
			logger.error("what happened", e);
		}

		return null;
	}*/

	/**
	 * Thread to listen packets
	 */
	@Override
	public void run() {
		try {
			if (!controller.activateGroup(group)) {
				logger.info("controller unavailable for group: " + group);
				return;
			}
			
			do {
				logger.debug("listening to packets ... ");
				// feed buffer until packet completed
				StringBuffer packet = new StringBuffer();
				int ret = 0;
				do {
					char[] buffer = new char[4096];
					ret = in.read(buffer);
					packet.append(new String(buffer));
				} while (ret!=-1 && !completedPacket.matcher(packet.toString()).find());
				
				logger.debug("about to handle packet ...");
				
				if (ret!=-1  && !done) {
					// We handle the packet
					RTSPPacket request = new RTSPPacket(packet.toString());
					RTSPResponse response = this.handlePacket(request);		
					logger.debug(request.toString());	
					logger.debug(response.toString());
		
			    	// Write the response to the wire
			    	try {			
			    		BufferedWriter oStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			    		oStream.write(response.getRawPacket());
			    		oStream.flush();
					} catch (IOException e) {
						logger.error("what happened", e);
					}
					
		    		if("TEARDOWN".equals(request.getReq())){
		    			socket.close();
		    			socket = null;
		    		}
				} else {
	    			socket.close();
	    			socket = null;
				}
			} while (socket!=null);
		} catch (IOException e) {
			logger.error("what happened", e);
		} finally {
			try {
				if (in!=null) in.close();
			} catch (IOException e) {
				logger.error("what happened", e);
			} finally {
				try {
					controller.deactivateGroup(group);					
					if (socket!=null) socket.close();
				} catch (IOException e) {
					logger.error("what happened", e);
				}
			}
		}

		logger.info("connection ended: " + uniqId); 
	}
}