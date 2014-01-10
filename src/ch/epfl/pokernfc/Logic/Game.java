package ch.epfl.pokernfc.Logic;

import java.util.LinkedList;

/***
 * This class includes the game logic.
 * @author Loic
 *
 */
public class Game {
	
	private LinkedList<Integer> mIds;
	
	
	public Game() {
		mIds = new LinkedList<Integer>();
	}
	
	/***
	 * Returns the next available player id and register this id.
	 * @return
	 */
	public int registerNextAvailablePlayerID() {
		if (mIds.isEmpty()) {
			mIds.add(1);
		} else {
			mIds.add(mIds.getLast() + 1);
		}
		return mIds.getLast();
	}
}
