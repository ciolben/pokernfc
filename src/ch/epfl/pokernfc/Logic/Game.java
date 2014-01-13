package ch.epfl.pokernfc.Logic;

import java.util.ArrayList;
import java.util.HashSet;

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
	private ArrayList<Integer> mIdsOrder; //for game order
	private int mIterator = 0;
	
	public Game() {
		mIds = new HashSet<Integer>();
		mIdsOrder = new ArrayList<Integer>();
	}
	
	/***
	 * Register a player id for the game logic.
	 * @return the new free id, -1 if no free slot.
	 */
	synchronized public int registerNextPlayerID() {
		if (mNumberOfPlayer == MAXPLAYER) { return -1; }
		++mNumberOfPlayer;
		
		if (mNumberOfPlayer == 1) {
			mIds.add(1);
			mIdsOrder.add(1);
			return 1;
		}
		
		int lastPlusOne = mIdsOrder.get(mNumberOfPlayer - 2); //-1 for 0-based, -1 for ++ before
		if (mIds.contains(Integer.valueOf(lastPlusOne))) {
			//look for a free id between 1 to MAXPLAYER
			if (mLastFreeId != -1 && !mIds.contains(Integer.valueOf(mLastFreeId))) {
				int id = mLastFreeId;
				mLastFreeId = -1;
				mIds.add(id);
				mIdsOrder.add(id);
				return id;
			}
			//costly, first players are likely to quit first
			for (int freeId = 1; freeId <= MAXPLAYER; ++freeId) {
				if (!mIds.contains(Integer.valueOf(freeId))) {
					mIds.add(freeId);
					mIdsOrder.add(freeId);
					return freeId;
				}
			}
			
			//should never happen (as we test everything bw 1 and max,
			//and number of player is below max before insertion.
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
			--mNumberOfPlayer;
		}
		return ok;
	}
	
	public int getNumberOfPlayer() {
		return mNumberOfPlayer;
	}
	
	/**
	 * Circular list of player, in order they should play.
	 * @return
	 */
	public int getNextPlayerID() {
		++mIterator;
		if (mIterator == mNumberOfPlayer) {
			mIterator = 0;
		}
		return mIdsOrder.get(mIterator);
	}
}
