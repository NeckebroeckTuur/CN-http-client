import java.io.File;


public class ClientMain {
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		String[] parsedUrl = URLParser.parse(args[1].trim());
		HttpCommand command;
		switch(args[0].toUpperCase()) {
			case "GET":
				command = HttpCommand.GET;
				break;
			default:
				throw new RuntimeException("Invalid request type.");
		}
		
		HttpClient client = new HttpClient(parsedUrl[0], command, Integer.valueOf(args[2]).intValue());
		File outputPath = new File("/home/tuur/Desktop/http/");
		client.setOutputPath(outputPath);
		client.sendGetRequest(parsedUrl[1], parsedUrl[2]);

		
		
						
		// TODO gebruik maken van de command line argumenten om de HttpClient aan te maken
			
		//HttpClient client = new HttpClient("http://neckebroecktuur.ulyssis.be", HttpCommand.GET, 80);
		//HttpClient client = new HttpClient("http://www.google.com/", HttpCommand.GET, 80);
		//HttpClient client = new HttpClient("http://webs.cs.berkeley.edu/", HttpCommand.GET, 80);
		//HttpClient client = new HttpClient("http://localhost", HttpCommand.GET, 80);
		//client.setOutputPath(outputPath);
		//client.sendGetRequest("/");
		//client.sendGetRequest("/");
		//client.closeConnection();
		System.out.println("----------\nFINISHED\n----------");
	}
	
	
}
