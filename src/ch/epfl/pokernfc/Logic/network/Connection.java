package ch.epfl.pokernfc.Logic.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import ch.epfl.pokernfc.Logic.network.Messages.MessageType;


public class Connection {
	private Socket mSocket;
	private long mLastSeen =0;
	private int mPlayerID;
	private BufferedReader inBuffer;
	private PrintWriter outPrinter;

	public Connection(Socket socket){
		mSocket = socket;
		try {
			inBuffer = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			outPrinter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket
					.getOutputStream())), true);

			
			//read somehow the id and other informations we need
			String line = readLine();
//			Log.d("ServerActivity", line);//////////
			Messages m = new Messages(line);
			if(MessageType.INIT == m.getType()){
				mPlayerID = Integer.valueOf(m.getLoad());
			}
			
			sendMessage(new Messages(MessageType.CARD, "server ack"));

			
			//connection ok:
			updateLastSeen();
			
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				mSocket = null;
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}


	public synchronized boolean sendMessage(Object message){
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

	public long getLastSeen() {
		return mLastSeen;
	}


	public synchronized void updateLastSeen() {
		this.mLastSeen = System.currentTimeMillis();
	}


	public synchronized boolean isAlive() {
		//TODO check somehow timeout

		if (mSocket == null){
			return false;
		}else if (mSocket.isClosed() || !mSocket.isConnected()) {
			return false;
		} else {
			return true;
		}
	}


	public int getmPlayerID() {
		return mPlayerID;
	}

}
