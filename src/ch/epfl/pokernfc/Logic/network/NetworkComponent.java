package ch.epfl.pokernfc.Logic.network;

import java.util.ArrayList;

/**
 * Ensure Server and Client can register a message handler.
 * @author Loic
 *
 */
public abstract class NetworkComponent extends Thread {
	private ArrayList<NetworkMessageHandler> mMessageHandlers = new ArrayList<NetworkMessageHandler>();
	
	public void registerNetworkMessageHandler(NetworkMessageHandler handler) {
		mMessageHandlers.add(handler);
	}
	
	public ArrayList<NetworkMessageHandler> getMessageHandlers() {
		return mMessageHandlers;
	}
}
