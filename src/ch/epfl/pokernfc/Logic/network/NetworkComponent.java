package ch.epfl.pokernfc.Logic.network;

/**
 * Ensure Server and Client can register a message handler.
 * @author Loic
 *
 */
public abstract class NetworkComponent extends Thread {
	private NetworkMessageHandler mMessageHandler;
	
	public void registerNetworkMessageHandler(NetworkMessageHandler handler) {
		mMessageHandler = handler;
	}
	
	public NetworkMessageHandler getMessageHandler() {
		return mMessageHandler;
	}
}
