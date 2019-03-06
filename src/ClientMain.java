import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;


public class ClientMain {
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		File outputFile = new File("/home/tuur/Desktop/httpresponse.txt");
			
		HttpClient client = new HttpClient("http://www.google.com", HTTPCommand.GET, 80);
		client.setDebugStream(System.out);
		client.setOutputFile(outputFile);
		client.sendHttpRequest("/");
		System.out.println("----------\nFINISHED\n----------");
		//client.closeConnection();	
	}
	
}
