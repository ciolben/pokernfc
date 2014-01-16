package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Logic.network.Client;
import ch.epfl.pokernfc.Logic.network.Server;

/**
 * Register the state of the application and perform routines
 * to keep a consistent state.
 * Methods are static so that no objects are ever destroyed if application
 * stops, resumes or is reopened (A VERIFIER)
 * @author Loic
 *
 */
public class PokerState {
	private static boolean isPlayer;
	public static void currentActivityIsPlayer(boolean state) { isPlayer = state; }
	public static boolean lastActivityWasPlayer() { return isPlayer; }

	private static Server gameServer = null;
	public static Server getGameServer() { if (gameServer == null) return gameServer = new Server(); return gameServer; }
	public static Server resetGameServer() { gameServer = null; return getGameServer(); }
	
	private static Client gameClient;
	public static Client createGameClient(int id, String ip, int port) { 
			if (gameClient == null) return gameClient = new Client(id, ip, port); return gameClient;
	}
	public static Client getGameClient() { return gameClient; }
}
