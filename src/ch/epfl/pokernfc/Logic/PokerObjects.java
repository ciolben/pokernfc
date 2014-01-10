package ch.epfl.pokernfc.Logic;

/***
 * Fabric for singleton game's objects.
 * @author Loic
 *
 */
public class PokerObjects {
	private static Pot potInstance = null;
	private static Player playerInstance = null;
	private static Game gameInstance = null;
	
	private PokerObjects() {}
	
	public static Pot getPot() {
		if (potInstance == null) {
			potInstance = new Pot();
		}
		return potInstance;
	}
	
	public static Player getPlayer() {
		if (playerInstance == null) {
			playerInstance = new Player();
		}
		return playerInstance;
	}
	
	public static Game getGame() {
		if (gameInstance == null) {
			gameInstance = new Game();
		}
		return gameInstance;
	}
}
