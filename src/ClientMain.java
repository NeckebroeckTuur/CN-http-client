import java.io.File;


public class ClientMain {
	// TODO https://github.com/arthurdecloedt/http-Project/blob/master/src/HTTPClient.java
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		File outputFile = new File("/home/tuur/Desktop/httpresponse.txt");
			
		HttpClient client = new HttpClient("http://neckebroecktuur.ulyssis.be", HttpCommand.GET, 80);
		client.setDebugStream(System.out);
		client.setOutputFile(outputFile);
		client.sendHttpRequest("/a.png");
		System.out.println("----------\nFINISHED\n----------");
		//client.closeConnection();	
	}
	
}
