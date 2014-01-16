package ch.epfl.pokernfc.Test;

import java.util.Scanner;

import ch.epfl.pokernfc.Logic.network.Client;
import ch.epfl.pokernfc.Logic.network.Message;
import ch.epfl.pokernfc.Logic.network.NetworkMessageHandler;

public class ClientTest {
	public static void main(String[] args) {
		ClientTestThread th = new ClientTestThread();
		th.start();
	}
}

class ClientTestThread extends Thread {

	public ClientTestThread() {
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		Client client;
		String entry;
		int ct = 2;
		int id;
		int port = 8765;
		String ip;
		try {
			System.out.println("Enter ip : ");
			ip = scanner.nextLine();
			System.out.println("Enter id : ");
			id = scanner.nextInt();
			client = new Client(id, ip, port);
			client.registerNetworkMessageHandler(new NetworkMessageHandler() {
				
				@Override
				public void handleMessage(Message message) {
					System.out.println("--> Message received : " + message);
					
				}
			});

			System.out.print("q to quit");
		} catch (Exception _) {
			System.err.println("Error in reading parameters/connecting to server.");
			scanner.close();
			return;
		} //128.179.130.87
		System.out.println("<-- Enter message : ");
		while (!(entry = scanner.nextLine()).equals("q")) {
			try {
				Message msg = new Message(entry);
				client.sendMessage(msg);
			} catch (Exception _) {
				System.out.println("Error. Try again.");
			}
			System.out.println("<-- Enter message : ");
		}
		scanner.close();
	}
}