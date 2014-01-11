package ch.epfl.pokernfc.Logic.network;


public class Messages{
	public static enum MessageType {
	    INIT, CARD, BID 
	}
	private static final String SPLITTER = "@";
	

	private String mLoad;
	private MessageType mType;
	
	public Messages(MessageType type, String load ) {
		mType = type;
		mLoad = load;
	}
	
	public Messages(String message){
		String[] s = message.split(SPLITTER);
		if(s.length == 2){
			mLoad = s[1];
			for (MessageType t : MessageType.values()) {
				if(s[0].equals(t.name())){
					mType = t;
					break;
				}
			}
			if (mType == null){
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
	
	public String toString(){
		return mType.name() + SPLITTER+mLoad;
	}
	
}
