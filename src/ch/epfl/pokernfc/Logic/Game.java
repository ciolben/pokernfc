package ch.epfl.pokernfc.Logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/***
 * This class includes the game logic.
 * @author Loic
 *
 */
public class Game {
	
	private final int MAXPLAYER = 100;
	private int mNumberOfPlayer = 0;
	private int mLastFreeId = -1; //for speeding up the search for a free id.
	private HashSet<Integer> mIds; //for O(1) search
	private LinkedList<Integer> mIdsOrder; //for game order
	
	public Game() {
		mIds = new HashSet<Integer>();
		mIdsOrder = new LinkedList<Integer>();
	}
	
	/***
	 * Register a player id for the game logic.
	 * @return the new free id, -1 if no free slot.
	 */
	synchronized public int registerNextPlayerID() {
		if (mNumberOfPlayer == MAXPLAYER) { return -1; }
		
		int lastPlusOne = mIdsOrder.getLast() + 1; 
		if (mIds.contains(Integer.valueOf(lastPlusOne))) {
			++mNumberOfPlayer;
			//look for a free id between 1 to MAXPLAYER
			if (mLastFreeId != -1 && !mIds.contains(Integer.valueOf(mLastFreeId))) {
				int id = mLastFreeId;
				mLastFreeId = -1;
				mIds.add(id);
				mIdsOrder.add(id);
				return id;
			}
			//costly, first players are likely to quit first
			for (int freeId = 1; freeId < MAXPLAYER; ++freeId) {
				if (!mIds.contains(Integer.valueOf(freeId))) {
					mIds.add(freeId);
					mIdsOrder.add(freeId);
					return freeId;
				}
			}
			
			//should never happen
			return -1;
			
		} else {
			mIds.add(lastPlusOne);
			mIdsOrder.add(lastPlusOne);
			return lastPlusOne;
		}
	}
	
	/***
	 * Remove player ID from the game.
	 * @param id
	 * @return true if player was present.
	 */
	synchronized public boolean revokePlayer(int id) {
		//Integer are compared by the equals method, thus by their value, not by their address.
		boolean ok = mIds.remove(Integer.valueOf(id));
		if (ok) {
			mIdsOrder.remove(Integer.valueOf(id));
			mLastFreeId = id;
		}
		return ok;
	}
}
