import java.io.File;


public class ClientMain {
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		
						
		// TODO gebruik maken van de command line argumenten om de HttpClient aan te maken
		
		File outputFile = new File("/home/tuur/Desktop/http/");
			
		//HttpClient client = new HttpClient("http://neckebroecktuur.ulyssis.be", HttpCommand.GET, 80);
		//HttpClient client = new HttpClient("http://www.google.com/", HttpCommand.GET, 80);
		HttpClient client = new HttpClient("http://localhost", HttpCommand.GET, 80);
		client.setOutputPath(outputFile);
		client.sendGetRequest("/");
		client.closeConnection();
		System.out.println("----------\nFINISHED\n----------");
	}
	
	
}
