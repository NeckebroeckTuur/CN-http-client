import java.io.File;
import java.util.Scanner;


public class ClientMain {
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
			return;
		}
		
		//String s = getShellInput("Insert the data for the put command:");
		
		String[] parsedUrl = UrlParser.parse(args[1].trim());
		HttpCommand command;
		switch(args[0].toUpperCase().trim()) {
			case "GET":
				command = HttpCommand.GET;
				break;
			case "POST":
				command = HttpCommand.POST;
				// TODO interactive shell
				break;
			case "PUT":
				command = HttpCommand.PUT;
				// TODO interactive shell
				break;
			default:
				throw new RuntimeException("Invalid request type.");
		}
		
		HttpClient client = new HttpClient(parsedUrl[0], command, Integer.valueOf(args[2]).intValue());
		File outputPath = new File("/home/tuur/Desktop/http/");
		client.setOutputPath(outputPath);
		
		// TODO sendGetRequest moet afhankelijk zijn van gegeven type request
		client.sendGetRequest(parsedUrl[1], parsedUrl[2]);
		client.closeConnection();
	}
	
	public static String getShellInput(String message) {
		Scanner sc = new Scanner(System.in);
		StringBuilder sBuilder = new StringBuilder();
		
		System.out.println(message);
		
		String in;
		while(!(in = sc.nextLine()).equals("")){
			sBuilder.append(in+"\r\n");
		}
		sc.close();
		
		String input = sBuilder.toString();
		return input.substring(0, input.length()-2);
	}
	
	
}
