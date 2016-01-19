
public class EmptyResponder extends BaseResponder {

	@Override
	public void run() { }

	@Override
	public RTSPResponse handlePacket(RTSPPacket packet) {
		return null;
	}
}
