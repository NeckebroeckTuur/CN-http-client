import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpClient {

	private Socket socket;
	private URI url;
	private HttpCommand command;
	private PrintWriter httpPrintWriter;
	
	//DEBUG
	private PrintStream UTFDebugStream;
	private boolean DEBUG = true;
	private File outputFile;
	
	private String LINE_SEPARATOR = "\r\n";
	
	public HttpClient(String url, HttpCommand command, int port) {
		socket = new Socket();
		try {
			this.url = new URI(String.format("%s:%d", url, port));
			this.command = command;
			this.socket = new Socket(InetAddress.getByName(this.url.getHost()), this.url.getPort());
			this.httpPrintWriter = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDebugStream(PrintStream debugStream) {
		this.UTFDebugStream = debugStream;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public void sendHttpRequest(String page) {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String.format("%s %s HTTP/1.1%s", command.getCommandString(), page, LINE_SEPARATOR));
		sBuilder.append(String.format("Host: %s:%d%sConnection: close%s%s", url.getHost(), url.getPort(), LINE_SEPARATOR, LINE_SEPARATOR, LINE_SEPARATOR));
		String requestString = sBuilder.toString();
		debugPrint(requestString);
		new HttpListener(this, this.outputFile);
		this.httpPrintWriter.println(requestString);
	}
	
	public void closeConnection() {
		this.httpPrintWriter.close();
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void debugPrint(String s) {
		if(UTFDebugStream != null && DEBUG) {
			this.UTFDebugStream.print(s);
		}
	}
	
}
