import java.io.File;


public class ClientMain {
	// TODO https://github.com/arthurdecloedt/http-Project/blob/master/src/HTTPClient.java
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Invalid use of chatclient.");
			System.out.println("Usage: ClientMain [HTTPCommand] [URI] [PORT]");
		}
		
		
		//htmlParseExample();
				
		// TODO gebruik maken van de command line argumenten om de HttpClient aan te maken
		
		File outputFile = new File("/home/tuur/Desktop/http/");
			
		HttpClient client = new HttpClient("http://neckebroecktuur.ulyssis.be", HttpCommand.GET, 80);
		//HttpClient client = new HttpClient("http://www.google.com/", HttpCommand.GET, 80);
		client.setDebugStream(System.out);
		client.setOutputPath(outputFile);
		client.sendGetRequest("/");
		System.out.println("----------\nFINISHED\n----------");
		//client.closeConnection();	
	}
	
	public static void htmlParseExample() {
		String html = "<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"<body>\n" + 
				"\n" + 
				"<h1>Astrophysics</h1>\n" + 
				"\n" + 
				"<img src=\"solar.jpg\" alt=\"Planet\" height=\"90\" width=\"90\">\n" + 
				"\n" + 
				"\n" + 
				"<p>Astrophysics is the branch of astronomy that employs the principles of physics and chemistry \"to ascertain the nature of the astronomical objects, rather than their positions or motions in space\". Among the objects studied are the Sun, other stars, galaxies, extrasolar planets, the interstellar medium and the cosmic microwave background. Emissions from these objects are examined across all parts of the electromagnetic spectrum, and the properties examined include luminosity, density, temperature, and chemical composition. Because astrophysics is a very broad subject, astrophysicists apply concepts and methods from many disciplines of physics, including mechanics, electromagnetism, statistical mechanics, thermodynamics, quantum mechanics, relativity, nuclear and particle physics, and atomic and molecular physics. [wikipedia]</p>\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"<img src=\"ad1.jpg\" alt=\"Ad1\" height=\"80\" width=\"100\">\n" + 
				"\n" + 
				"<img src=\"ad2.jpg\" alt=\"Ad2\" height=\"42\" width=\"100\">\n" + 
				"\n" + 
				"\n" + 
				"<p>Observational astronomy is a division of the astronomical science that is concerned with recording data, in contrast with theoretical astrophysics, which is mainly concerned with finding out the measurable implications of physical models. It is the practice of observing celestial objects by using telescopes and other astronomical apparatus. [wikipedia] </p>\n" + 
				"\n" + 
				"<img src=\"robot.jpg\" alt=\"Robot\" height=\"200\" width=\"200\">\n" + 
				"\n" + 
				"<img src=\"ad3.jpg\" alt=\"Ad3\" height=\"200\" width=\"189\">\n" + 
				"\n" + 
				"\n" + 
				"</body>\n" + 
				"</html>";
		
		String[] srces = HttpResponse.findSources(html, true);
		for(String s: srces) {
			System.out.println(s);
		}
		
	}
	
}
