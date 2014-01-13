package ch.epfl.pokernfc.Logic;

import java.util.ArrayList;
import java.util.HashSet;

import ch.epfl.pokernfc.PokerState;
import ch.epfl.pokernfc.Logic.network.Message;
import ch.epfl.pokernfc.Logic.network.NetworkMessageHandler;
import ch.epfl.pokernfc.Logic.network.Server;

/***
 * This class includes the game logic used by the Pot.
 * @author Loic
 *
 * State machine for PokerNFC
 * 
 * START game
 * -Determine BUTTON position
 * -Player at left of the BUTTON give SMALLBLIND
 * -Player at left of the SMALLBLIND give BIGBLIND
 * -START distributing cards
 * --from SMALLBLIND, left (clockwise normally) 1x
 * --2x
 * -START PREFLOP tour
 * --from BIGBLIND, left (clockwise)
 * -community CARD : +3 (3 / 5) (FLOP)
 * -START BID tour
 * --from BUTTON, left (clockwise)
 * -community CARD : +1 (4 / 5) (TURN)
 * -START BID tour
 * --from BUTTON, left (clockwise)
 * -community CARD : +1 (5 / 5) (RIVER)
 * -START BID tour
 * --from BUTTON, left (clockwise)
 * -if AT LEAST two players alive, find the best hand for winner, else last player is winner.
 * 
 *        2     3     
 *     _____________
 *    /             \
 *   |    o      s   | 
 * 1 |            B  | 4
 *   |               |
 *    \_____________/
 * 
 *       6       5
 * 
 * PREFLOP tour :
 * ACTIONS : FOLLOW, RAISE, FOLD
 * start from 5
 * then like BID tour.
 * 
 * BID tour :
 * ACTIONS : CHECK, FOLLOW, RAISE, FOLD
 * start from 3 (CHECK, RAISE, FOLLOW)
 * 4 - 2 : CHECK, FOLLOW, RAISE, FOLD
 *
 */
public class Game {
	
	private final int MAXPLAYER = 100;
	private int mNumberOfPlayer = 0;
	private int mLastFreeId = -1; //for speeding up the search for a free id.
	private HashSet<Integer> mIds; //for O(1) search
	private ArrayList<Integer> mIdsOrder; //for game order
	private int mIterator = 0;
	
	//communication
	private NetworkMessageHandler mMessageHandler = null;
	
	//state fields
	private enum TOUR {
		PREFLOP, BID
	}
	private TOUR mCurrentTour;
	private int mLastDealer = -1;
	private enum BIDTYPE {
		SMALL, BIG, BID
	}
	private BIDTYPE mCurrentBidType;
	
	public Game() {
		mIds = new HashSet<Integer>();
		mIdsOrder = new ArrayList<Integer>();
	}
	
	//game settings
	private int mSmallBlind = 5;
	private int mBigBlind = 10;
	
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
	 * @return id
	 */
	public int getNextPlayerID() {
		++mIterator;
		if (mIterator == mNumberOfPlayer) {
			mIterator = 0;
		}
		return mIdsOrder.get(mIterator);
	}

	public int getCurrentPlayerID() {
		return mIdsOrder.get(mIterator);
	}
	
	/**
	 * Logic for Server
	 * @return true if the game has started.
	 */
	public boolean startGame() {
		if (mNumberOfPlayer < 3) {
			System.out.println("Number of players less than 3");
			//TODO : maybe support 2 players
			return false;
		}
		if (mMessageHandler == null) {
			createMessageHandler();
		}
		
		//determine who's Dealer...
		mLastDealer = mLastDealer + 1;
		
		//...and the one for the small blind
		mIterator = mLastDealer + 1;
		mCurrentBidType = BIDTYPE.SMALL;
		
		//start preflop tour
		mCurrentTour = TOUR.PREFLOP;
		
		//ask small blind first (and so start the state machine)
		PokerState.getGameServer().sendMessage(getCurrentPlayerID(),
				new Message(Message.MessageType.ASKBLIND, String.valueOf(mSmallBlind)));
		
		return true;
	}
	
	private void preflop() {
		Server server = PokerState.getGameServer();
		
		switch (mCurrentBidType) {
		case SMALL:
			
			break;
		case BIG:
			
			break;
		case BID:
			
			break;
		}
	}
	
	/**
	 * Manage messages from Client for Game logic
	 */
	private void createMessageHandler() {
		mMessageHandler = new NetworkMessageHandler() {
			
			@Override
			public void handleMessage(Message message) {
				switch (message.getType()) {
				case UNKNOWN:
					/*do nothing*/
					break;
				case BID:
					break;
				default:

				}
			}
		};
		PokerState.getGameServer().registerNetworkMessageHandler(mMessageHandler);
	}
}
