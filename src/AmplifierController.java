import java.io.IOException;
import java.util.Set;

public interface AmplifierController {
	/**
	 * @return the volume of the computer audio used as line out to the russound controller
	 */
	float getLineOutKnobVolume();

	/**
	 * 
	 * @return the set of groups that controller is configured for
	 */
	Set<String> getGroups();

	/**
	 * close the controller
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	boolean activateGroup(String name) throws IOException;
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	boolean deactivateGroup(String name) throws IOException;
	
	/**
	 * sets the volume for the current active group
	 * 
	 * @param knobVolume value from 0 to 1.0 representing the volume
	 * @return
	 * @throws IOException
	 */
	boolean setVolume(float knobVolume) throws IOException;
}
