import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpClient {

	private Socket socket;
	private URI url;
	private HTTPCommand command;
	private DataOutputStream out;
	
	public HttpClient(String url, HTTPCommand command, int port) {
		socket = new Socket();
		try {
			this.url = new URI(String.format("%s:%d", url, port));
			this.command = command;
			this.socket = new Socket(InetAddress.getByName(this.url.getHost()), this.url.getPort());
	    	out = new DataOutputStream(this.socket.getOutputStream());

		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequest() {
		try {
			out.writeUTF(String.format("%s %s %d HTTP/1.1", command.getCommandString(), url.getHost(), url.getPort()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
}
