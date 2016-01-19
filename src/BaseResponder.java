import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Security;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An primitive RTSP responder for replying iTunes
 * @author bencall
 *
 */
public abstract class BaseResponder extends Thread {
	final Logger logger = LoggerFactory.getLogger(BaseResponder.class);
	
	protected volatile boolean done = false;
	
	// Pre-define patterns
	protected static final Pattern authPattern = Pattern.compile("Digest username=\"(.*)\", realm=\"(.*)\", nonce=\"(.*)\", uri=\"(.*)\", response=\"(.*)\"");
	protected static final Pattern completedPacket = Pattern.compile("(.*)\r\n\r\n");
	
	protected static final String key =  
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
		+"-----END RSA PRIVATE KEY-----\n"; 


	/**
	 * These volumes come from the iphone over raop protocol
	 */
	protected static class VolumeTranslator {
		public static int MAX_KNOB_VOLUME = 16;
		private static double[] iPhoneVolumes = {
				-144.000, -28.125, -26.250, -24.375, -22.500, -20.625, -18.750, -16.875, 
				-15.000, -13.125, -11.250, -9.375, -7.500, -5.625, -3.750, -1.875, 0.000
		};

		/**
		 * 
		 * @param raopVolume value coming from RAOP protocol
		 * @return float value from 0 to 1.0
		 */
		public static float getKnobVolume(float raopVolume) {
			for (int i = 0; i < iPhoneVolumes.length; i++) {
				if (iPhoneVolumes[i] >= raopVolume) {
					return (float)i/(float)(iPhoneVolumes.length-1);
				}
			}
			
			return 0.0F;
		}
		
		public static float getRaopVolume(float knobVolume) {
			if (knobVolume > 1.0F) return 0.0F;
			int idx = (int)(knobVolume * iPhoneVolumes.length-1);
			return (float)iPhoneVolumes[idx];
			
		}
	}

	public synchronized void stopResponder() {
		done = true;
	}
	
	public abstract RTSPResponse handlePacket(RTSPPacket packet);

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
	 */
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
	}

	/**
	 * Decrypt with RSA priv key
	 * @param array
	 * @return
	 */
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
	}

	/**
	 * Thread to listen packets
	 */
	public abstract void run();
}