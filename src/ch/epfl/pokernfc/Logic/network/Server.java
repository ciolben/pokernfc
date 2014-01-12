package ch.epfl.pokernfc.Logic.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//import android.os.Handler;
//import android.util.Log;
//import android.widget.Toast;


public class Server extends Thread {

//	private Handler mHandler;

	private static final int SERVERPORT = 8765;
	private static String serverIP;
	private ServerSocket serverSocket;
	private AtomicBoolean mClose = new AtomicBoolean(false);
	private ReentrantLock lock = new ReentrantLock();
	private Thread listenNewPlayerThread = null;
	private AtomicInteger awaitingNewPlayers = new AtomicInteger(0);

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

			//while not close  check all msocket from time to time... sync the socket
			while (!mClose.get()) {
				// LISTEN FOR INCOMING CLIENTS

				for (Connection connection : mSockets.values()) {
					synchronized (connection) {
						if (!connection.isAlive()){
							//remove from list
							mSockets.remove(connection.getmPlayerID());
							continue;
						}
						String line = null;
						while (connection.messageAvailable() && (line = connection.readLine()) != null) {
//							Log.d("ServerActivity", line);//////////


							// DO WHATEVER YOU WANT TO THE FRONT END
							// THIS IS WHERE YOU CAN BE CREATIVE
							System.out.println("server received: "+ line);


//							mHandler.post(new Runnable() {
//								@Override
//								public void run() {
//									// DO WHATEVER YOU WANT TO THE FRONT END
//									// THIS IS WHERE YOU CAN BE CREATIVE
//								}
//							});
							connection.updateLastSeen();
						}
						try {
							sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
					if (!inetAddress.isLoopbackAddress()) { 

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
		}
		

		if(listenNewPlayerThread == null){
		listenNewPlayerThread = new Thread(


				new Runnable() {

					@Override
					public void run() {
						System.out.println("waiting for new player...");

						if (serverIP != null) {
							
						while (!mClose.get()){
							if (awaitingNewPlayers.get() <= 0){
								try {
									synchronized (awaitingNewPlayers) {
										awaitingNewPlayers.wait();
									}
									if(mClose.get()) break;
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						try {
									
								
								lock.lock();
								// LISTEN FOR INCOMING CLIENT
								Socket client = serverSocket.accept();

								lock.unlock();
								
								
								Connection connection = new Connection(client, id);	
								//connection successfully established with the client: store the socket
								mSockets.put(connection.getmPlayerID(), connection);
								
								awaitingNewPlayers.decrementAndGet();
							
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

				mSockets.remove(connection.getmPlayerID());
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

	public Connection receiveMessage(int id){
		Connection connection = mSockets.get(id);
		if (connection == null){
			return null;
		}
		synchronized (connection) {

			if (!connection.isAlive()) {
				mSockets.remove(connection.getmPlayerID());
				return null;
			} else {
				return connection;
			}
		}
	}

	public void close(){
		mClose.set(true);
		synchronized (awaitingNewPlayers) {
			awaitingNewPlayers.notifyAll();
		}
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