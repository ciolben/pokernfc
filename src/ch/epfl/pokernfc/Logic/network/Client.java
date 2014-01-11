package ch.epfl.pokernfc.Logic.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import ch.epfl.pokernfc.Logic.network.Messages.MessageType;

//import android.os.Handler;
//import android.util.Log;

public class Client extends Thread {
	private static String mServerIP;
	private static int mServerPort = 8765;
	private boolean mConnected;
	//private Handler handler;
	private BufferedReader inBuffer;
	private PrintWriter outPrinter;
	private Socket mSocket;
	private int myID;

	public Client(int id, String serverIP, int serverPort/*, Handler handler*/){
//		this.handler = handler;
		mServerIP = serverIP;
		myID = id;
		mConnected = false;
		this.start();

	}

	public void run() {
		try {
			InetAddress serverAddr = InetAddress.getByName(mServerIP);
			mSocket = new Socket(serverAddr, mServerPort);
			
			outPrinter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket
					.getOutputStream())), true);
			inBuffer = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

			
			//send first message to server: registration blabla
			outPrinter.println(new Messages(MessageType.INIT, myID+""));
			System.out.println("client "+ myID+" send init");


			
			mConnected = true;
			//wait for other messages from server
			while (mConnected) {
				try {
//					Log.d("ClientActivity", "C: Sending command.");
					
					
					// WHERE YOU ISSUE THE COMMANDS
					//if(messageAvailable()){
					System.out.println("client "+myID+" received: "+ readLine());
					//}
					
//					Log.d("ClientActivity", "C: Sent.");
					sleep(5000);
				} catch (Exception e) {
//					Log.e("ClientActivity", "S: Error", e);
					e.printStackTrace();
				}
			}
			mSocket.close();
			//Log.d("ClientActivity", "C: Closed.");
		} catch (Exception e) {
			//Log.e("ClientActivity", "C: Error", e);
			e.printStackTrace();
			mConnected = false;
		}
	}
	
	
	public boolean sendMessage(Object message){
		if(outPrinter != null){
		outPrinter.println(message);
		return true;
		} else {
			return false;
		}
}

	public synchronized String readLine(){
		try {
			if(inBuffer != null){
			return inBuffer.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
	public synchronized boolean messageAvailable(){
		try {
			return mSocket.getInputStream().available() >0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;


	}
}