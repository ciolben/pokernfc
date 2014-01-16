package ch.epfl.pokernfc.Test;

import java.util.ArrayList;
import java.util.Scanner;

import ch.epfl.pokernfc.Logic.Game;
import ch.epfl.pokernfc.Logic.network.Message;

/***
 * Class for testing game logic
 * @author Loic
 *
 */
public class GameTest {
	public static void main(String[] args) {
		Game game = new Game();
		
		System.out.println("register players :");
		ArrayList<Integer> players = new ArrayList<Integer>();
		for (int i = 0; i < 4 ; ++i) {
			int id = game.registerNextPlayerID();
			System.out.println("added player : " + id);
			players.add(id);
		}
		
		game.startGame();
		Tester tester = new Tester(game);
		tester.start();
	}
}

class Tester extends Thread {
	Game game;
	Server server;
	
	public Tester(Game game) {
		this.game = game;
		server = PokerState.getGameServer();
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		String entry;
		int ct = 2;
		System.out.print("enter src : (q to quit) ");
		while (!(entry = scanner.nextLine()).equals("q")) {
			if (entry.isEmpty()) {
				Message msg = new Message("BID@10");
				++ct;
				msg.setSource(ct);
				System.out.println("--> src : " + ct + " msg : BID@10");
				server.sendMessageToGame(msg);
				if (ct > 3) {
					ct = 0;
				}
				continue;
			}
			try {
				int source = Integer.parseInt(entry);
				System.out.println("enter msg : ");
				Message msg = new Message(scanner.nextLine());
				msg.setSource(source);
				server.sendMessageToGame(msg);
				System.out.println("-----------------------");
			} catch (NumberFormatException _) {
				continue;
			} finally {
				System.out.print("enter src : ");
			}
		}
		scanner.close();
	}
}

class PokerState {
	static Server server = new Server();
	public static Server getGameServer() {
		return server;
	}
}

class Server {
	NetworkMessageHandler handler;
	public boolean sendMessage(int id, Message message) {
		System.out.println("Message sent to player " + id + " with msg : " + message);
		return true;
	}
	public void registerNetworkMessageHandler(NetworkMessageHandler handler) {
		this.handler = handler;
	}
	//for communication
	public void sendMessageToGame(Message message) {
		handler.handleMessage(message);
	}
}

interface NetworkMessageHandler {

	public void handleMessage(Message message);
	
}

class PokerObjects {
	static Pot pot = new Pot();
	public static Pot getPot() {
		return pot;
	}
}

class Pot {
	float cash = 0;
	public void addCash(float v) { cash += v; }
}