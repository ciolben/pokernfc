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
		PREFLOP, FLOP, TURN, RIVER
	}
	private TOUR mCurrentTour;
	private int mLastDealer = -1;
	private enum BIDTYPE {
		SMALL, BIG, BID
	}
	private BIDTYPE mCurrentBidType;
	private float mCurrentBid;
	private ArrayList<Float> mPlayerBids;
	private int mConsecutiveFollow;
	private ArrayList<Integer> mForfeited;
	
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
	 * Forfeited players are not in the list.
	 * @return id, or -1 if no player is registered or able to play
	 */
	public int getNextPlayerID() {
		if (mNumberOfPlayer == 0) { return -1; }
		if (mForfeited.size() == mNumberOfPlayer) { return -1; }
		
		++mIterator;
		if (mIterator == mNumberOfPlayer) {
			mIterator = 0;
		}
		
		//at least one will match before iterator == numberofplayer
		while (mForfeited.contains(mIdsOrder.get(mIterator))) {
			++mIterator;
		}
		
		return mIdsOrder.get(mIterator);
	}

	public int getCurrentPlayerID() {
		return mIdsOrder.get(mIterator);
	}
	
	private float getCurrentPlayerBid() {
		return mPlayerBids.get(mIterator);
	}
	
	private void addToCurrentPlayerBid(float amount) {
		mPlayerBids.set(mIterator, mPlayerBids.get(mIterator) + amount);
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
		
		//init state vars
		mConsecutiveFollow = 0;
		mForfeited = new ArrayList<Integer>();
		mPlayerBids = new ArrayList<Float>();
		mPlayerBids.ensureCapacity(mIdsOrder.size());
		for (int i = 0; i < mIdsOrder.size(); ++i) {
			mPlayerBids.add(0.f);
		}
		
		//determine who's Dealer...
		mLastDealer = mLastDealer + 1;
		
		//...and the one for the small blind
		mIterator = mLastDealer + 1;
		mCurrentBidType = BIDTYPE.SMALL;
		
		//start preflop tour
		mCurrentTour = TOUR.PREFLOP;
		
		//will ask small blind first (and so start the state machine)
		preflop(0);
		
		return true;
	}
	
	private void preflop(float value) {
		Server server = PokerState.getGameServer();
		
		switch (mCurrentBidType) {
		case SMALL:
			//check if the current bid = small blind
			if (value == mSmallBlind) {
				mCurrentBid = value;
				PokerObjects.getPot().addCash(mSmallBlind);
				//we can proceed to big blind
				mCurrentBidType = BIDTYPE.BIG;
				mCurrentBid = 0;
				//save bid for player
				addToCurrentPlayerBid(mSmallBlind);
				//move to next player
				getNextPlayerID();
				preflop(-1);
			} else {
				if (value > 0) {
					server.sendMessage(getCurrentPlayerID(),
							new Message(Message.MessageType.REFUND, String.valueOf(value)));
				}
				server.sendMessage(getCurrentPlayerID(),
					new Message(Message.MessageType.ASKBLIND, String.valueOf(mSmallBlind)));
			}
			break;
		case BIG:
			//check if the current bid = small blind
			if (value == mBigBlind) {
				mCurrentBid = value;
				PokerObjects.getPot().addCash(mBigBlind);
				//we can proceed to standard bid
				mCurrentBidType = BIDTYPE.BID;
				mConsecutiveFollow = 1;
				//save bid for player
				addToCurrentPlayerBid(mBigBlind);
				//move to next player
				getNextPlayerID();
				bidtour(-1);
			} else {
				if (value > 0) {
					server.sendMessage(getCurrentPlayerID(),
							new Message(Message.MessageType.REFUND, String.valueOf(value)));
				}
				server.sendMessage(getCurrentPlayerID(),
					new Message(Message.MessageType.ASKBLIND, String.valueOf(mBigBlind)));
			}
			break;
		case BID:
			//if first beginning of second tour and small blind -> must bid mcurrentbid - smallblind
			//lastDealer + 1 = small blind position
//			if (!mFirstRoundEnded && (getCurrentPlayerID() == mLastDealer + 1)) {
//				mFirstRoundEnded = true;
//				
//				if (value >= mCurrentBid - mSmallBlind) {
//					if (value > mCurrentBid - mSmallBlind) {
//						mConsecutiveFollow = 0;
//					} else { //=
//						++mConsecutiveFollow;
//					}
//
//					mCurrentBid = value + mSmallBlind; //or getCurrentPlayerBid(), we add the rest
//					PokerObjects.getPot().addCash(value);
//
//					//determine if we need to continue the bidtour
//					if (mConsecutiveFollow == mNumberOfPlayer - mForfeited.size()
//							|| mForfeited.size() == mNumberOfPlayer - 1) { //one player left
//						mConsecutiveFollow = 0;
//						mCurrentTour = TOUR.FLOP;
//						System.out.println("Start of FLOP tour");
//						System.out.println("Link new 3 cards view HERE");
//					} else {
//						//save bid for player
//						addToCurrentPlayerBid(value);
//						//move to next player
//						getNextPlayerID();
//						bidtour(0); //we g to bid tour now
//					}
//				} else if (value != 0) {
//					//if player has not bid enough, refund it and ask again
//					server.sendMessage(getCurrentPlayerID(),
//							new Message(Message.MessageType.REFUND, String.valueOf(value)));
//					server.sendMessage(getCurrentPlayerID(),
//							new Message(Message.MessageType.ASKBID, String.valueOf(mCurrentBid - mSmallBlind)));
//				} else {
//					//ask for bid
//					server.sendMessage(getCurrentPlayerID(),
//							new Message(Message.MessageType.ASKBID, String.valueOf(mCurrentBid - mSmallBlind)));
//				}
//			} else {
//				bidtour(value);
//			}
			bidtour(value);
		}
		
	}
	
	private void bidtour(float value) {
		Server server = PokerState.getGameServer();
		
		if (mCurrentBidType != BIDTYPE.BID) {
			System.err.println("ERROR bidtour (BIDTYPE != BID)");
			return;
		}
		
		//accept only bid >= mCurrentBid means that FOLLOW or RAISE happened
		float diff = mCurrentBid - getCurrentPlayerBid();
		if (value >= diff) {
			if (value > diff) {
				mConsecutiveFollow = 0;
			} else {
				++mConsecutiveFollow;
			}
			
			mCurrentBid += value - diff;
			PokerObjects.getPot().addCash(value);
			
			//determine if we need to continue the bidtour
			if (mConsecutiveFollow == mNumberOfPlayer - mForfeited.size() + 1
					|| mForfeited.size() == mNumberOfPlayer - 1) { //one player left
				mConsecutiveFollow = 0;
				//next card or determine winner
				switch (mCurrentTour) {
				case PREFLOP:
					mCurrentTour = TOUR.FLOP;
					System.out.println("Start of FLOP tour");
					System.out.println("Link new 3 cards view HERE");
					break;
				case FLOP:
					mCurrentTour = TOUR.TURN;
					System.out.println("Start of TURN tour");
					System.out.println("Link new card view HERE");
					break;
				case TURN:
					mCurrentTour = TOUR.RIVER;
					System.out.println("Start of RIVER tour");
					System.out.println("Link new card view HERE");
					break;
				case RIVER:
					System.out.println("Determining who is winner");
					break;
				}
				//must start left from dealer
				mIterator = mLastDealer; //and not +1 because :
				getNextPlayerID(); //this will ensure that the 1st to play has not fold
				//reset the bid
				mCurrentBid = 0;
			} else {
				System.out.println("current follow : " + mConsecutiveFollow);
				//save bid for player
				addToCurrentPlayerBid(value);
				//move to next player
				getNextPlayerID();
				bidtour(-1);
			}
		} else if (value > 0) {
			//if player has not bid enough, refund it and ask again
			server.sendMessage(getCurrentPlayerID(),
					new Message(Message.MessageType.REFUND, String.valueOf(value)));
			server.sendMessage(getCurrentPlayerID(),
					new Message(Message.MessageType.ASKBID, String.valueOf(diff)));
		} else {
			//ask for bid, like value = -1
			server.sendMessage(getCurrentPlayerID(),
					new Message(Message.MessageType.ASKBID, String.valueOf(diff)));
		}
	}
	
	/**
	 * Check if the player id provided can play now.
	 * @param id
	 * @return true if it is his turn.
	 */
	private boolean checkTurn(int id) {
		return id == getCurrentPlayerID();
	}
	
	/**
	 * Manage messages from Client for Game logic
	 */
	private void createMessageHandler() {
		mMessageHandler = new NetworkMessageHandler() {
			
			@Override
			public void handleMessage(Message message) {

				//check if current player has the right to play
				if (!checkTurn(message.getSource())) {
					//TODO : refund maybe
					System.out.println("Wrong player turn : " + message.getSource() + ", expected : " + getCurrentPlayerID());
					return;
				}
				
				switch (message.getType()) {
				case UNKNOWN:
					/*do nothing*/
					break;
				case BID:
					//used for small and big blind
					if (mCurrentTour == TOUR.PREFLOP) {
						preflop(Float.valueOf(message.getLoad()));
				    //standard bid
					} else if (mCurrentTour == TOUR.FLOP
							|| mCurrentTour == TOUR.TURN
							|| mCurrentTour == TOUR.RIVER) {
						bidtour(Float.valueOf(message.getLoad()));
					}
					break;
				case FOLD:
					//cannot fold if small or big blind
					if (mCurrentBidType == BIDTYPE.SMALL
							|| mCurrentBidType == BIDTYPE.BIG) {
						preflop(-1); //ask again blind
					} else {
						mForfeited.add(message.getSource());
						getNextPlayerID();
						
						if (mCurrentTour == TOUR.PREFLOP) {
							preflop(-1);
						} else if (mCurrentTour == TOUR.FLOP
								|| mCurrentTour == TOUR.TURN
								|| mCurrentTour == TOUR.RIVER) {
							bidtour(-1);
						}
					}
				default:

				}
			}
		};
		PokerState.getGameServer().registerNetworkMessageHandler(mMessageHandler);
	}
}
