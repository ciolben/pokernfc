package ch.epfl.pokernfc.Logic.network;

import java.util.Scanner;

import ch.epfl.pokernfc.Logic.network.Messages.MessageType;


public class javaTest {
 public static void main(String[] args) {
	Server s = new Server();
	
	Client t[] = new Client[6];
	for (int i =1; i<= 5; i++){
	t[i] = new Client(i, "127.0.0.1", 8765);
	s.listenToNewPlayer();
	}
	
	
	Scanner scanner = new Scanner(System.in);
	while(true){

        System.out.print("Enter a sentence:\t");
        String sentence = scanner.nextLine();
        if(sentence.equals("s")){
            sentence = scanner.nextLine();
            s.sendMessage(Integer.valueOf(sentence), new Messages(MessageType.CARD, "22"));
        } else {
        	System.out.println("p "+sentence+" wrinting");
        	int i = Integer.valueOf(sentence);
        	t[i].sendMessage(new Messages(MessageType.BID, "34 "+i));
        }
	}
}
}