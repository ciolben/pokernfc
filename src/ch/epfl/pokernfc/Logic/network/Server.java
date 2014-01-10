package ch.epfl.pokernfc.Logic.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class Server extends Thread {

	private Handler mHandler;

	private static final int SERVERPORT = 8765;
	private static String serverIP;
	private ServerSocket serverSocket;
	private boolean mClose = false;
	private ReentrantLock lock = new ReentrantLock();

	private ConcurrentHashMap<Integer, Socket> mSockets = new ConcurrentHashMap<Integer, Socket>();


	public static int getServerPort() {
		return SERVERPORT;
	}

	public static String getServerIP() {
		return serverIP;
	}

	public Server(Handler handler){
		mHandler = handler;

		serverIP = getLocalIpAddress();
		try {
			serverSocket = new ServerSocket(SERVERPORT);
			this.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		if (serverIP != null) {
			writeToastMessage("Listening on IP: " + serverIP, Toast.LENGTH_SHORT);

			//while not close  check all msocket from time to time... sync the socket
			while (!mClose) {
				// LISTEN FOR INCOMING CLIENTS
				for (Socket client : mSockets.values()) {
					if (client == null){
						continue;
					}
					synchronized (client) {

						if (client.isClosed() || !client.isConnected()) {
							continue;
						}

						try {
							BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

							String line = null;
							while ((line = in.readLine()) != null) {
								Log.d("ServerActivity", line);//////////


								// DO WHATEVER YOU WANT TO THE FRONT END
								// THIS IS WHERE YOU CAN BE CREATIVE


								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// DO WHATEVER YOU WANT TO THE FRONT END
										// THIS IS WHERE YOU CAN BE CREATIVE
									}
								});
							}
						} catch (Exception e) {
							writeToastMessage("Oops. Connection interrupted! check your connection!", Toast.LENGTH_LONG);
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			writeToastMessage("Couldn't detect connection.", Toast.LENGTH_LONG);
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
		Log.e("ServerActivity", ex.toString());
	}
	return null;
}

public void  listenToNewPlayer(){

	//c'est un peut debile... mieux faire seulement un thread qui fait un while sur serversocket.accept mais bon...
	(new Thread(


			new Runnable() {

				@Override
				public void run() {


					try {
						if (serverIP != null) {
							lock.lock();
							serverSocket = new ServerSocket(SERVERPORT);
							// LISTEN FOR INCOMING CLIENT
							Socket client = serverSocket.accept();

							serverSocket.close();
							lock.unlock();
							//
							try {
								int id = 0;
								BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
								String line = null;

								while ((line = in.readLine()) != null) {
									Log.d("ServerActivity", line);//////////


									//somehow read the id, and check i'm waiting for it...
									id = 0; //TODO line.balbal

									// DO WHATEVER YOU WANT TO THE FRONT END
									// THIS IS WHERE YOU CAN BE CREATIVE

								}

								//connection successfully established with the client: store the socket
								mSockets.put(id, client);

							} catch (Exception e) {
								writeToastMessage("Oops. Connection interrupted! check your connection!", Toast.LENGTH_LONG);
								e.printStackTrace();
							}
						} else {
							writeToastMessage("Couldn't detect connection.", Toast.LENGTH_LONG);

						}
					} catch (Exception e) {
						if(lock.isHeldByCurrentThread()){
							lock.unlock();
						}
						writeToastMessage("Errror", Toast.LENGTH_LONG);
						e.printStackTrace();
					}

				}
			}
			)).start();

}

public boolean sendMessage(int id, String outgoingMsg){
	Socket client;
	synchronized (mSockets) {
		client = mSockets.get(id);
	}
	if (client == null){
		return false;
	}

	synchronized (client) {

		if (client.isClosed() || !client.isConnected()) {
			return false;
		}

		try {
			//write the message:
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			out.write(outgoingMsg);
			out.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}

public BufferedReader receiveMessage(int id){
	Socket client;
	synchronized (mSockets) {
		client = mSockets.get(id);
	}
	if (client == null){
		return null;
	}

	synchronized (client) {

		if (client.isClosed() || !client.isConnected()) {
			return null;
		}

		try {
			//write the message:
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			return in;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

public void close(){
	mClose = true;
}


private void writeToastMessage(final String message, final int length  ){
	mHandler.post(new Runnable() {
		@Override
		public void run() {
			//Toast.makeText(getApplicationContext(), message , length ).show();
		}
	});
}

}