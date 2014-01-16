package ch.epfl.pokernfc.Logic.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import ch.epfl.pokernfc.Logic.network.Message.MessageType;


public class Client extends NetworkComponent {
	private static String mServerIP;
	private static int mServerPort = 8765;
	private boolean mConnected;
	private BufferedReader inBuffer;
	private PrintWriter outPrinter;
	private Socket mSocket;
	private int myID;

	public Client(int id, String serverIP, int serverPort){
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
			outPrinter.println(new Message(MessageType.INIT, myID+""));
			System.out.println("client "+ myID+" send init");


			
			mConnected = true;
			//wait for other messages from server
			while (mConnected) {
				try {
					String content = readLine();
					System.out.println("client "+myID+" received: "+ content);
					if (content != null) {
						Message message = new Message(content);
						if (message.getType() == MessageType.PING){
							sendMessage(new Message(MessageType.PING, "PONG wella!!"));
						} else {
							//give content to player
							for (NetworkMessageHandler handler : getMessageHandlers()) {
								handler.handleMessage(message);
							}
						}
					} else {
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			mSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			mConnected = false;
		} finally {
			localSend(new Message(Message.MessageType.ERROR, "Disconnected"));
			System.out.println("Disconnected.");
		}
	}
	
	/**
	 * Send locally a message to the client.
	 * @param message
	 */
	public void localSend(Message message) {
		for (NetworkMessageHandler handler : getMessageHandlers()) {
			handler.handleMessage(message);
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