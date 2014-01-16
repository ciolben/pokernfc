package ch.epfl.pokernfc.Logic.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.conn.util.InetAddressUtils;

import ch.epfl.pokernfc.Logic.network.Message.MessageType;



public class Server extends NetworkComponent {


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

	public Server(){
		
		serverIP = getLocalIpAddress();
		try {
			serverSocket = new ServerSocket(SERVERPORT);
			System.out.println("serverip: "+serverIP);
			this.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		if (serverIP != null){

			//while not close  check all msocket from time to time... sync the socket
			while (!mClose.get()) {
				try {
					//some sleep is always good
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				for (Connection connection : mSockets.values()) {
					synchronized (connection) {
						if (!connection.isAlive()){
							//remove from list
							mSockets.remove(connection.getPlayerID());
							Message m = new Message(MessageType.FOLD, "byebye!!");
							m.setSource(connection.getPlayerID());
							localSend(m);
							connection.close();
							continue;
						}
						String line = null;
						
						while (connection.messageAvailable() && (line = connection.readLine()) != null) {


								System.out.println("server received: "+ line);
								Message message = new Message(line);
							if (message.getType() != MessageType.PING) {
								
								message.setSource(connection.getPlayerID());
									for (NetworkMessageHandler handler : getMessageHandlers()) {
										handler.handleMessage(message);
								}
							}

							connection.updateLastSeen();
						}
						long time = System.currentTimeMillis();
						if((time - connection.getLastSeen()) > connection.TIMEOUT){
							if ((time - connection.getLastSeen()) > connection.TIMEOUT * 4){
								mSockets.remove(connection.getPlayerID());
								Message m = new Message(MessageType.FOLD, "byebye!!");
								m.setSource(connection.getPlayerID());
								localSend(m);
								connection.close();
							} else if (time - connection.getLastPing() > connection.TIMEOUT /2) {
								sendMessage(connection.getPlayerID(), new Message(MessageType.PING, "are you still alive??"));
								connection.updatePing();
							}
						}
					}
				}
			}
		} else {
//			Couldn't detect connection
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
								Socket client = serverSocket.accept();

								lock.unlock();
								
								
								Connection connection = new Connection(client);
								if(mWaitingPlayerConnection.contains(connection.getPlayerID())){
									//connection successfully established with the client: store the socket
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
							e.printStackTrace();
						}
						}
						} else {
//							Couldn't detect connection

						
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
			System.out.println("MESSAGE SENT TO " + id + " | content : " + outgoingMsg);
			writeOK = connection.sendMessage(outgoingMsg);
			return writeOK;

		}
	}

	public void closeNewPlayer(){
		mNewPlayerClose.set(true);
		synchronized (awaitingNewPlayers) {
			awaitingNewPlayers.notifyAll();
		}
		if(listenNewPlayerThread != null){
			listenNewPlayerThread.interrupt();
		}
		listenNewPlayerThread =null;
		
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

	/**
	 * Return the list of connected ids.
	 * @return
	 */
	public List<Integer> getConnectedIds() {
		return new ArrayList<Integer>(mSockets.keySet());
	}
	
	public void closeConnection(int id) {
		Connection conn = mSockets.get(id);
		if (conn != null) {
			conn.close();
		}
	}
	
	/**
	 * Locally send the message to himself.
	 * @param message
	 */
	public void localSend(Message message) {
		for (NetworkMessageHandler handler : getMessageHandlers()) {
			handler.handleMessage(message);
		}
	}

}
