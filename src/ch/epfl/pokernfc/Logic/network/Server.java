package ch.epfl.pokernfc.Logic.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.spec.DESKeySpec;

import org.apache.http.conn.util.InetAddressUtils;

import ch.epfl.pokernfc.Logic.network.Message.MessageType;
import ch.epfl.pokernfc.Logic.texasholdem.Card;
import ch.epfl.pokernfc.Logic.texasholdem.Deck;

//import android.os.Handler;
//import android.util.Log;
//import android.widget.Toast;


public class Server extends NetworkComponent {

//	private Handler mHandler;

	private static final int SERVERPORT = 8765;
	private static String serverIP;
	private ServerSocket serverSocket;
	private AtomicBoolean mClose = new AtomicBoolean(false);
	private AtomicBoolean mNewPlayerClose = new AtomicBoolean(false);

	private ReentrantLock lock = new ReentrantLock();
	private Thread listenNewPlayerThread = null;
	private AtomicInteger awaitingNewPlayers = new AtomicInteger(0);

	private Set<Integer> mWaitingPlayerConnection = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>());
	private ConcurrentHashMap<Integer, Connection> mSockets = new ConcurrentHashMap<Integer, Connection>();

	public int getServerPort() {
		return SERVERPORT;
	}

	public String getServerIP() {
		return serverIP;
	}

	public Server(/*Handler handler*/){
		//mHandler = handler;
		
		serverIP = getLocalIpAddress();
		try {
			serverSocket = new ServerSocket(SERVERPORT);
			System.out.println("serverip: "+serverIP);
			this.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		if (serverIP != null){
//			writeToastMessage("Listening on IP: " + serverIP, Toast.LENGTH_SHORT);

			//TODO test::
			Deck deck = new Deck();
			
			
			//while not close  check all msocket from time to time... sync the socket
			while (!mClose.get()) {
				try {
					//some sleep is always good
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (Connection connection : mSockets.values()) {
					synchronized (connection) {
						if (!connection.isAlive()){
							//remove from list
							mSockets.remove(connection.getPlayerID());
							continue;
						}
						String line = null;
						//deck.shuffleDeck();
						for (Card card : deck.getCards()) {
							sendMessage(connection.getPlayerID(), new Message(MessageType.CARD1, card.getValue().getSuitValue()+"_"+card.getSuit().getSuitValue()));

						}
						
						while (connection.messageAvailable() && (line = connection.readLine()) != null) {
//							Log.d("ServerActivity", line);//////////

							// DO WHATEVER YOU WANT TO THE FRONT END
							// THIS IS WHERE YOU CAN BE CREATIVE
							System.out.println("server received: "+ line);
							if (line != null) {
								//give content to Pot
								for (NetworkMessageHandler handler : getMessageHandlers()) {
									Message message = new Message(line);
									message.setSource(connection.getPlayerID());
									handler.handleMessage(message);
								}
							}

//							mHandler.post(new Runnable() {
//								@Override
//								public void run() {
//									// DO WHATEVER YOU WANT TO THE FRONT END
//									// THIS IS WHERE YOU CAN BE CREATIVE
//								}
//							});
							connection.updateLastSeen();
						}
					}
				}
			}
		} else {
//			writeToastMessage("Couldn't detect connection.", Toast.LENGTH_LONG);
		}
	}


	//get ip of the local network
	private String getLocalIpAddress() {
		
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) { 

						return inetAddress.getHostAddress().toString(); }
				}
			}
		} catch (SocketException ex) {
//			Log.e("ServerActivity", ex.toString());
			ex.printStackTrace();
		}
		return null;
	}

	/***
	 * Start listening to Player with the specified id.
	 * @return
	 */
	public void listenToNewPlayer(final int id){

		synchronized (awaitingNewPlayers) {
			awaitingNewPlayers.incrementAndGet();
			awaitingNewPlayers.notifyAll();
			mWaitingPlayerConnection.add(id);
		}
		

		if(listenNewPlayerThread == null){		
	
		
		listenNewPlayerThread = new Thread(


				new Runnable() {


					@Override
					public void run() {
						System.out.println("waiting for new player...");

						if (serverIP != null) {
							
						while (!mNewPlayerClose.get()){
							if (awaitingNewPlayers.get() <= 0){
								try {
									synchronized (awaitingNewPlayers) {
										awaitingNewPlayers.wait();
									}
									if(mNewPlayerClose.get()) break;
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						try {
									
								
								lock.lock();
								// LISTEN FOR INCOMING CLIENT
								Socket client = serverSocket.accept();

								lock.unlock();
								
								
								Connection connection = new Connection(client);
								if(mWaitingPlayerConnection.contains(connection.getPlayerID())){
									//connection successfully established with the client: store the socket
//									mWaitingPlayerConnection.remove(connection.getPlayerID());
									mSockets.put(connection.getPlayerID(), connection);
									awaitingNewPlayers.decrementAndGet();
								} else {
									//I was not waiting this player close this connection and forget everything about it
									connection.close();
								}
								
							
						} catch (Exception e) {
							if(lock.isHeldByCurrentThread()){
								lock.unlock();
							}
//							writeToastMessage("Errror", Toast.LENGTH_LONG);
							e.printStackTrace();
						}
						}
						} else {
//							writeToastMessage("Couldn't detect connection.", Toast.LENGTH_LONG);

						
						}

					}
				}
				);
		
		listenNewPlayerThread.start();
		}
	}

	public boolean sendMessage(int id, Object outgoingMsg){
		Connection connection = mSockets.get(id);

		if (connection == null){
			return false;
		}

		synchronized (connection) {

			if (!connection.isAlive()) {

				mSockets.remove(connection.getPlayerID());
				return false;
			}
			boolean writeOK = false;
			//write the message:
			writeOK = connection.sendMessage(outgoingMsg);
			if(writeOK) {
					connection.updateLastSeen();
			}
			return writeOK;

		}
	}

//	public Connection receiveMessage(int id){
//		Connection connection = mSockets.get(id);
//		if (connection == null){
//			return null;
//		}
//		synchronized (connection) {
//
//			if (!connection.isAlive()) {
//				mSockets.remove(connection.getPlayerID());
//				return null;
//			} else {
//				return connection;
//			}
//		}
//	}

	public void closeNewPlayer(){
		mNewPlayerClose.set(true);
		synchronized (awaitingNewPlayers) {
			awaitingNewPlayers.notifyAll();
		}
		if(listenNewPlayerThread != null){
			listenNewPlayerThread.interrupt();
		}
		listenNewPlayerThread =null;
		
		// maybe return player inside mWaitingPlayerConnection hence the one that didn't connect properly
		mWaitingPlayerConnection.clear();
		
	}
	
	public void close(){
		mClose.set(true);
		closeNewPlayer();
		for (Connection c : mSockets.values()) {
			c.close();
		}
		mSockets.clear();
	}


	private void writeToastMessage(final String message, final int length  ){
//		if(mHandler != null){
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				//Toast.makeText(getApplicationContext(), message , length ).show();
//			}
//		});
//		}
	}

}
