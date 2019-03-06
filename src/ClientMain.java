import javax.sound.midi.Soundbank;

public class ClientMain {
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		HttpClient client = new HttpClient("http://www.google.com", HTTPCommand.GET, 80);
		client.sendRequest();
		System.out.println("----------\nFINISHED\n----------");
	}
	
}
