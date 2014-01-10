package ch.epfl.pokernfc;

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
}
