import javax.sound.midi.Soundbank;

public class ClientMain {
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		ChatClient client = new ChatClient("http://www.google.com", HTTPCommand.GET, 80);
		client.sendRequest();
		System.out.println("----------\nFINISHED\n----------");
	}
	
}
