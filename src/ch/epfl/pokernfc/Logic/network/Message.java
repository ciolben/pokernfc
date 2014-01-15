package ch.epfl.pokernfc.Logic.network;

/**
 * Class to handle the type of messages between the server and clients.
 *
 */
public class Message{
	public static enum MessageType {
		//enum		descr						sens (server <--> player)
	    UNKNOWN, 	//unknown message			<-->
	    INIT,		//
	    CARD1,		//send card1  				-->
	    CARD2,		//send card2  				-->
	    BID,		//send a bid				<--
	    FOLD,		//player fold				<--
	    ASKBLIND,	//ask for small/big blind	-->
	    ASKBID,		//ask player action			-->
	    REFUND,		//send money back			-->
	    ACK			//Acknowledgment			-->
	}
	private static final String SPLITTER = "@";
	

	private String mLoad;
	private MessageType mType;
	private int mSrc;
	
	public Message(MessageType type, String load ) {
		mType = type;
		mLoad = load;
		mSrc = -1;
	}
	
	public Message(String message){
		String[] s = message.split(SPLITTER);
		if(s.length == 2){
			mLoad = s[1];
			mType = MessageType.UNKNOWN;
			for (MessageType t : MessageType.values()) {
				if(s[0].equals(t.name())){
					mType = t;
					break;
				}
			}
			if (mType == MessageType.UNKNOWN){
				System.err.println("unknown type");
			}
		}  else {
			System.err.println("unreadable message found length: "+s.length+" message: "+message);
		}
	}
	
	
	public String getLoad() {
		return mLoad;
	}

	public MessageType getType() {
		return mType;
	}
	
	public void setSource(int playerID) {
		mSrc = playerID;
	}
	
	public int getSource() {
		return mSrc;
	}
	
	public String toString(){
		return mType.name() + SPLITTER+mLoad;
	}
	
}
