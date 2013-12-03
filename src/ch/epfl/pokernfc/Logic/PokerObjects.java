package ch.epfl.pokernfc.Logic;

public class PokerObjects {
	private static Pot potInstance = null;
	private static Player playerInstance = null;
	
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
}
