package ch.epfl.pokernfc.Logic.network;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class Client extends Thread {
	private static String mServerIP;
	private static int mServerPort = 8765;
	private boolean mConnected;
	private Handler handler = new Handler();

	public Client(String serverIP, int serverPort, Handler handler){
		this.handler = handler;
		mServerIP = serverIP;
		mConnected = false;

	}

	public void run() {
		try {
			InetAddress serverAddr = InetAddress.getByName(mServerIP);
			Log.d("ClientActivity", "C: Connecting...");
			Socket socket = new Socket(serverAddr, mServerPort);
			mConnected = true;
			while (mConnected) {
				try {
					Log.d("ClientActivity", "C: Sending command.");
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
							.getOutputStream())), true);
					
					// WHERE YOU ISSUE THE COMMANDS
					
					
					out.println("Hey Server!");
					Log.d("ClientActivity", "C: Sent.");
					sleep(5000);
				} catch (Exception e) {
					Log.e("ClientActivity", "S: Error", e);
				}
			}
			socket.close();
			Log.d("ClientActivity", "C: Closed.");
		} catch (Exception e) {
			Log.e("ClientActivity", "C: Error", e);
			mConnected = false;
		}
	}
}