package ch.epfl.pokernfc.Utils;

/***
 * Create useful messages for communication.
 * @author Loic
 *
 */
public class MessageUtils {
	private static final char SEPARATOR = ',';
	
	public static String createNFCWelcome(String ip, int port, int id) {
		return ip + SEPARATOR + port + SEPARATOR + id;
	}
	
	/***
	 * Returns at position
	 * 	0 : ip as String
	 * 	1 : port as int
	 * 	2 : player id as int
	 * @param message
	 * @return null if an error occured.
	 */
	public static Object[] parseNFCWelcomeMessage(String message) {
		if (message.isEmpty()) { return null; }
		String[] content = message.split(new String(new char[] {SEPARATOR}));
		
		if (content.length < 3) { return null; }
		Object[] result = new Object[3];
		result[0] = content[0];
		try {
			result[1] = Integer.parseInt(content[1]);
			result[2] = Integer.parseInt(content[2]);
			return result;
		} catch (NumberFormatException _) { return null; }
	}
}
